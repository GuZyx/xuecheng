package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import com.xuecheng.framework.web.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户认证显示层
 */
@RestController
@RequestMapping("/")
public class AuthController extends BaseController implements AuthControllerApi{
    @Autowired
    AuthService authService;

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Override   //登录
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if(loginRequest==null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        } else if(StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //账号
        String username = loginRequest.getUsername();
        //密码
        String password = loginRequest.getPassword();
        //申请令牌
        AuthToken authToken = authService.login(username,password,clientId,clientSecret);

        //将令牌存在cookie
        String access_token = authToken.getAccess_token();
        this.saveCookie(access_token);
        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    //将令牌存储到cookie
    private void saveCookie(String token){
        //HttpServletResponse response,String domain,String path, String name,
        //                String value, int maxAge,boolean httpOnly
//        HttpServletResponse response = ((ServletRequestAttributes)
//                RequestContextHolder.getRequestAttributes()).getResponse();

        CookieUtil.addCookie(this.response,cookieDomain,"/","uid",token,
                cookieMaxAge,false);
    }

    //从cookie删除token
    private void clearCookie(String token){
        CookieUtil.addCookie(this.response,cookieDomain,"/","uid",token,
                0,false);
    }

    @Override   //退出
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //删除redis中的token
        String token = this.getTokenFromCookie();
        authService.delToken(token);
        //删除cookie
        this.clearCookie(token);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override   //查询用户的Jwt令牌
    @GetMapping("/userjwt")
    public JwtResult userJwt() {
        //取出cookie中的用户身份令牌
        String uid = this.getTokenFromCookie();
        if(uid==null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        //拿着身份令牌从redis中查询jwt令牌
        AuthToken authToken = authService.getUserToken(uid);
        if(authToken==null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        String jwt_token = authToken.getJwt_token();
        //将jwt返回给用户
        return new JwtResult(CommonCode.SUCCESS,jwt_token);
    }

    //取出cookie中的用户身份令牌
    private String getTokenFromCookie(){
        Map<String, String> map = CookieUtil.readCookie(this.request, "uid");
        if(map==null){
            return null;
        }else {
            if(StringUtils.isEmpty(map.get("uid"))){
                return null;
            }
        }
        return map.get("uid");
    }
}
