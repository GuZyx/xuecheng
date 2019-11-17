package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.ext.UserTokenStore;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证业务层
 */
@Service
public class AuthService {
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${auth.tokenValiditySeconds}")
    private long ttl; //redis超时时间

    //用户认证申请令牌，将令牌存储到redis
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //请求spring security申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if(authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //用户身份的令牌
        String access_token = authToken.getAccess_token();
        //内容
        String content = JSON.toJSONString(authToken);

        //将令牌存储到redis
        boolean saveToken = this.saveToken(access_token, content, ttl);
        if(!saveToken){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        return authToken;
    }

    /**
     * 存储令牌到redis
     * @param access_token 用户身份令牌
     * @param content   内容(AuthToken对象)
     * @param ttl   过期时间
     * @return  返回true或false
     */
    private boolean saveToken(String access_token,String content,long ttl){
        String key = "user_token:"+access_token;
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire>0;
    }

    //从redis查询令牌
    public AuthToken getUserToken(String token){
        String key = "user_token:"+token;
        //从redis中取到的值
        String value = stringRedisTemplate.opsForValue().get(key);
        //转成对象
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //删除token
    public void delToken(String access_token){
        String key = "user_token:"+access_token;
        stringRedisTemplate.delete(key);
    }

    //获取令牌信息
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        //从Eureka中获取认证服务的实例地址(因为spring security在认证服务中)
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //此地址就是 http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUri = uri+"/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String,String> header = new LinkedMultiValueMap<>();
        //拿到basic串
        String httpBasic = this.getHttpBasic(clientId,clientSecret);
        header.add("Authorization",httpBasic);

        //定义body
        LinkedMultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);

        //设置restTemplate远程调用时，对400和401不让报错，正常返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401 ){
                    super.handleError(response);
                }
            }
        });
        ResponseEntity<Map> exchange = restTemplate.exchange(authUri, HttpMethod.POST, httpEntity, Map.class);
        //申请令牌的信息
        Map bodyMap = exchange.getBody();
        if(bodyMap==null || StringUtils.isEmpty(bodyMap.get("access_token")) ||
                StringUtils.isEmpty(bodyMap.get("refresh_token")) ||
                StringUtils.isEmpty(bodyMap.get("jti"))){
            if(bodyMap!=null && bodyMap.get("error_description")!= null){
                String error_description = (String) bodyMap.get("error_description");
                if(error_description.contains("UserDetailsService returned null")){
                    //账号不存在
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }else if(error_description.contains("坏的凭证")){
                    //密码错误
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            return null;
        }

        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti")); //用户身份令牌，短令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));   //刷新token
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));  //jwt令牌
        return authToken;
    }

    //获取http的basic串
    private String getHttpBasic(String clientId, String clientSecret) {
        String s = clientId+":"+clientSecret;
        //将这个串进行Base64编码
        byte[] encode = Base64Utils.encode(s.getBytes());
        return "Basic "+new String(encode);
    }


}
