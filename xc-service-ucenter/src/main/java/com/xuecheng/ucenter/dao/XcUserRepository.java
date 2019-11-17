package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//用户中心持久层
@Repository
public interface XcUserRepository extends JpaRepository<XcUser,String> {
    //根据用户名查询用户信息
    XcUser findByUsername(String username);
}
