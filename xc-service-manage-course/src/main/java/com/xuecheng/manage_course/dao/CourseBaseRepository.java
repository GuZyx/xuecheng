package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator.
 * CourseBase持久层--jpa
 */
@Repository
public interface CourseBaseRepository extends JpaRepository<CourseBase,String> {
}
