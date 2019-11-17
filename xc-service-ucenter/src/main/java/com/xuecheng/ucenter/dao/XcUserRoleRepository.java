package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 * 用户与角色关联表持久层
 */
public interface XcUserRoleRepository extends JpaRepository<XcUserRole, String> {
    List<XcUserRole> findXcUserRoleByUserId(String userId);
}
