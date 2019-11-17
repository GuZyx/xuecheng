package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.TeachplanControllerApi;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程计划视图层
 */
@RestController
@RequestMapping("/course/teachplan")
public class TeachplanController implements TeachplanControllerApi {
    @Autowired
    TeachplanService teachplanService;

    //查看课程计划
    //当客户拥有了course_teachplan_list权限时可访问此方法
    @PreAuthorize("hasAuthority('course_teachplan_list')")
    @Override
    @GetMapping("/list/{courseId}")
    public TeachplanNode findTeachPlanList(@PathVariable("courseId") String courseId) {
        return teachplanService.findTeachPlanList(courseId);
    }

    //添加课程计划
    @PreAuthorize("hasAuthority('course_teachplan_add')")
    @Override
    @PostMapping("/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return teachplanService.addTeachplan(teachplan);
    }
}
