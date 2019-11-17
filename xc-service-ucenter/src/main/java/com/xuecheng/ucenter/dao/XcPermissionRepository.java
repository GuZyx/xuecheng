package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcPermission;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * 权限持久层
 */
public interface XcPermissionRepository extends JpaRepository<XcPermission, String> {

}
