package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

//配置信息持久层
@Repository
public interface CmsConfigRepository extends MongoRepository<CmsConfig,String> {
}
