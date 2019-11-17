package com.xuecheng.auth.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(XcServiceList.XC_SERVICE_UCENTER)
public interface UserClient {
    //根据用户名传用户信息
    @GetMapping("/ucenter/getuserext")
    public XcUserExt getUserExt(@RequestParam("username") String username);
}
