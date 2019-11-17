package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageServiceTest {
    @Autowired
    private CmsPageService cmsPageService;

    //测试三元
    @Test
    public void testOptional(){
        CmsPage byId = cmsPageService.findById("5a9620b9b00ffc5a9cdebaed");
        System.out.println(byId);
    }

    @Test
    public void testEdit(){
        CmsPage cmsPage = new CmsPage();
        cmsPage.setDataUrl("aaa");
        CmsPageResult result = cmsPageService.update("5d9c4db44336b53a64a4c32a", cmsPage);
        System.out.println(result.getCmsPage());
    }

    @Test   //测试页面静态化
    public void testHtml(){
        String staticHtml = cmsPageService.getPageHtml("5d9ef1c80bf95b37bc612c32");
        System.out.println(staticHtml);
    }

}
