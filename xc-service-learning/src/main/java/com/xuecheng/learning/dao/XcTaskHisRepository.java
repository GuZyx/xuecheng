package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.task.XcTaskHis;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 历史记录持久层
 */
public interface XcTaskHisRepository extends JpaRepository<XcTaskHis,String> {
    //根据用户id和客户id查询

}
