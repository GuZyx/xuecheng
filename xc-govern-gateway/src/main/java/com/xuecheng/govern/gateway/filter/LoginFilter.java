package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component  //登录过滤器(zuul身份校验)
public class LoginFilter extends ZuulFilter {
    @Autowired
    private AuthService authService;

    @Override   //过滤器的类型
    public String filterType() {
        /**
         * pre：请求在被路由之前调用
         *
         * routing：在路由请求时调用
         *
         * error：处理请求发生错误时调用
         *
         * post：在routing和error过滤器之后调用
         *
         */
        return "pre";
    }

    @Override   //过滤器序号，越小越被优先执行
    public int filterOrder() {
        return 0;
    }

    @Override   //是否要执行这个过滤器
    public boolean shouldFilter() {
        //false代表不执行
        return true;
    }

    //过滤器的内容
    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = requestContext.getRequest();
        //得到response
        HttpServletResponse response = requestContext.getResponse();

        //取cookie中的身份令牌
        String access_token = authService.getTokenFromCookie(request);
        if(StringUtils.isEmpty(access_token)){ //cookie为空
            //拒绝访问
            this.access_denied(requestContext,response);
            return null;
        }

        //从header中取jwt
        String jwt = authService.getJwtFromHeader(request);
        if(StringUtils.isEmpty(jwt)){ //jwt令牌为空
            this.access_denied(requestContext,response);
            return null;
        }

        //从redis中取出jwt的过期时间
        long expire = authService.getExpire(access_token);
        if(expire<0){ //剩余时间小于0
            this.access_denied(requestContext,response);
            return null;

        }

        return null;
    }

    //拒绝访问
    private void access_denied(RequestContext requestContext,HttpServletResponse response){
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置响应代码
        requestContext.setResponseStatusCode(200);
        //构建响应信息
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        //转成json
        String body = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(body);
        //转成json，需要设置contentType
        response.setContentType("application/json;charset=utf-8");
    }
}
