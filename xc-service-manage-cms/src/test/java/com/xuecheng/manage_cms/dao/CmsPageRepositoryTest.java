package com.xuecheng.manage_cms.dao;


import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;


    @Test
    public void testFindAll(){  //查询全部
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    @Test
    public void testFindPage(){  //分页查询
        //分页参数
        int page = 0;   //从0开始
        int size = 1;   //查询一条
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(pageable);
        System.out.println(cmsPages);
    }

    @Test
    public void testInsert(){   //测试添加
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageName("测试添加");
        cmsPage.setSiteId("s01");
        cmsPage.setPageId("t01");
        //添加参数列表
        List<CmsPageParam> cmsPageParams = new ArrayList<>();
        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");
        cmsPageParams.add(cmsPageParam);
        cmsPage.setPageParams(cmsPageParams);
        //保存
        CmsPage result = cmsPageRepository.save(cmsPage);
        System.out.println(result);   //打印保存的结果
    }

    @Test
    public void testDelete(){   //测试删除
        cmsPageRepository.deleteById("t01");
    }

    @Test
    public void testUpdate(){   //测试更改
        //查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById("t01");
        if(optional.isPresent()){   //判断是否为空
            CmsPage cmsPage = optional.get();
            //设置要更改的值
            cmsPage.setPageName("测试更改");
            //修改
            CmsPage result = cmsPageRepository.save(cmsPage);
            System.out.println(result); //打印一下结果
        }
    }

    @Test
    public void testFindByName(){   //自定义方法查询
        //根据名称查询
        Optional<CmsPage> optional = cmsPageRepository.findByPageName("测试更改");
        if(optional.isPresent()){   //判断是否为空
            //获取对象
            CmsPage cmsPage = optional.get();
            System.out.println(cmsPage);
        }
    }

    @Test
    public void testFindByExample(){    //自定义条件查询(Example)
        int page = 0;   //从0开始
        int size = 10;   //查询一条
        Pageable pageable = PageRequest.of(page,size);

        //  条件值对象   cmsPage
        CmsPage cmsPage = new CmsPage();
//        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");  //设置站点id siteId
//        cmsPage.setTemplateId("5a925be7b00ffc4b3c1578b5");  //设置模板id
        cmsPage.setPageAliase("图");  //设置页面别名模糊查询

        //  条件匹配器    matcher
//        ExampleMatcher matcher= ExampleMatcher.matching();
        //定义匹配器     ExampleMatcher.GenericPropertyMatchers.contains() 必须要包含
//        matcher = matcher.withMatcher("pageAliase",
//                ExampleMatcher.GenericPropertyMatchers.contains().endsWith());
        ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("pageAliase"
                ,ExampleMatcher.GenericPropertyMatchers.startsWith().endsWith());


        //定义Example     .of(条件值对象，条件匹配器)
        Example<CmsPage> example = Example.of(cmsPage,matcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> list = all.getContent();
        System.out.println(list);
    }

    @Test
    public void testEdit(){
        Optional<CmsPage> byId = cmsPageRepository.findById("5d909573db96174010333f2e");
        CmsPage cmsPage = byId.get();
        cmsPage.setDataUrl("aaa");
        cmsPageRepository.save(cmsPage);
        System.out.println(cmsPage);
    }

}
