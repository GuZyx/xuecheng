package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //从头取出jwt令牌
    public String getJwtFromHeader(HttpServletRequest request){
        //取出头信息
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        //判断是不是以Bearer 开头
        if(!authorization.startsWith("Bearer ")){
            return null;
        }
        //取到jwt令牌,从第7个截取(因为Bearer 开头)
        return authorization.substring(7);
    }

    //从cookie取出token
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String access_token = map.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    //判断令牌是否在redis过期
    public long getExpire(String access_token){
        String key = "user_token:"+access_token;
        //获取剩余时间
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

}
