package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeachplanRepository extends JpaRepository<Teachplan,String> {
    //根据课程id和parent来查 查询teachplan SELECT * FROM teachplan WHERE courseid = '4028e581617f945f01617f9dabc40000' AND parentid ='0';
    public List<Teachplan> findByCourseidAndAndParentid(String courseid,String parentid);
}
