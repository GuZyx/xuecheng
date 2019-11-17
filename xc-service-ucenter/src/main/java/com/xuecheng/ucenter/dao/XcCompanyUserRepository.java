package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 公司与用户交互持久层
 */
@Repository
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser,String> {
    //根据userId查询公司信息
    XcCompanyUser findByUserId(String userId);
}
