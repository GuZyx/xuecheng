package com.xuecheng.manage_cms.dao;


import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_cms.service.CmsSiteService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsSiteRepositoryTest {

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    private CmsSiteService cmsSiteService;

    @Test
    public void testFindAll(){  //查询全部
        List<CmsSite> all = cmsSiteRepository.findAll();
        System.out.println(all);
    }

    @Test
    public void testService(){  //service查询全部
        QueryResponseResult all = cmsSiteService.findAll();
        System.out.println(all.getQueryResult().getList());
        System.out.println(all.getQueryResult().getTotal());
    }

}
