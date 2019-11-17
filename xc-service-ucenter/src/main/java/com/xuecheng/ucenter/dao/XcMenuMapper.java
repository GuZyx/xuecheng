package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * mybatis用户权限持久层
 */
@Mapper
@Repository
public interface XcMenuMapper {
    //根据用户id查询查询用户的权限
    public List<XcMenu> selectPermissionsByUserId(String userId);
}
