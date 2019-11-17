package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcRole;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * 角色持久层
 */
public interface XcRoleRepository extends JpaRepository<XcRole, String> {

}
