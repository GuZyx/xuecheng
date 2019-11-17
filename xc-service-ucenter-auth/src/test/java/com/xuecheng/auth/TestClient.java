package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    //远程请求springSecurity获取令牌
    @Test
    public void testClient(){
        //从Eureka中获取认证服务的实例地址(因为spring security在认证服务中)
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //此地址就是 http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUri = uri+"/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String,String> header = new LinkedMultiValueMap<>();
        //拿到basic串
        String httpBasic = this.getHttpBasic("XcWebApp", "XcWebApp");
        header.add("Authorization",httpBasic);

        //定义body
        LinkedMultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");
        body.add("redirect_uri","http://localhost/");

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
        System.out.println(bodyMap);

    }
    //获取http的basic串
    public String getHttpBasic(String clientId,String clientSecret){
        String s = clientId+":"+clientSecret;
        //将这个串进行Base64编码
        byte[] encode = Base64Utils.encode(s.getBytes());
        return "Basic "+new String(encode);
    }

}
