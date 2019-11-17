package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageViewControllerApi;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 页面预览，继承BaseController只是为了获取response、request、cookie
 */
@Controller
public class CmsPagePreviewController extends BaseController implements CmsPageViewControllerApi {

    @Autowired
    private CmsPageService cmsPageService;

    //页面预览
    @GetMapping("/cms/preview/{pageId}")
    public void preview(@PathVariable("pageId") String pageId) {
        response.setHeader("content-type","text/html;charset=utf-8");
        //执行静态化
        String pageHtml = cmsPageService.getPageHtml(pageId);
        //通过response将文件输出
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            outputStream.write(pageHtml.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
