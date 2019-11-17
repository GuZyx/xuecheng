package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

//配置信息业务层
@Service
public class CmsConfigService {
    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    //根据configId获取模板信息
    public CmsConfig getModelById(String configId){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(configId);
        return optional.orElse(null);
    }

}
