package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Component    测试用的
public class LoginFilterTest extends ZuulFilter {

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
    //测试的需求：过滤所有需求，判断头部信息是否有Authorization，如果没有则拒绝访问，否则转发到微服务
    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = currentContext.getRequest();
        //得到response
        HttpServletResponse response = currentContext.getResponse();
        //得到Authorization头部信息
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            //拒绝访问
            currentContext.setSendZuulResponse(false);
            //设置响应代码
            currentContext.setResponseStatusCode(200);
            //构建响应信息
            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
            //转成json
            String body = JSON.toJSONString(responseResult);
            currentContext.setResponseBody(body);
            //转成json，需要设置contentType
            response.setContentType("application/json;charset=utf-8");
        }
        return null;
    }
}
