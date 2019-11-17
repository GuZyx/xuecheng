package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 学习与课程关联持久层
 */
public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse,String> {
    //根据用户id和客户id查询
    XcLearningCourse findByUserIdAndCourseId(String userId,String courseId);
}
