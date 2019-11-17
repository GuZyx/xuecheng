package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * 用户菜单持久层
 */
public interface XcMenuRepository extends JpaRepository<XcMenu, String> {
}
