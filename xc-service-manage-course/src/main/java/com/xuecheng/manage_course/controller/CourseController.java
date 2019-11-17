package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.response.DeleteCourseResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理前端
 */
@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi{
    @Autowired
    private CourseService courseService;

    @Override   //新增课程
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    @Override   //分页查询全部我的课程
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable(name = "page") int page,
                                                          @PathVariable(name = "size") int size,
                                                          CourseListRequest courseListRequest) {
        //
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);
        if(userJwt==null){
            ExceptionCast.cast(CommonCode.UNAUTHENTICATED);
        }
        //要拿到当前用户所属单位的id
        String company_id = userJwt.getCompanyId();
        courseListRequest.setCompanyId(company_id);
        return courseService.findAll(page,size,courseListRequest);
    }

    @Override   //查看课程基本信息
    @GetMapping("/coursebase/get/{id}")
    public CourseBase getCourseBaseById(@PathVariable("id") String courseId) {
        return courseService.getCourseBaseById(courseId);
    }

    @Override   //更新课程基本信息
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id,@RequestBody CourseBase courseBase) {
        return courseService.updateCourseBase(id,courseBase);
    }

    @Override   //获取课程营销信息
    @GetMapping("/courseMarket/get/{id}")
    public CourseMarket getCourseMarketById(@PathVariable("id") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    @Override   //更新课程营销信息
    @PutMapping("/courseMarket/update/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id,@RequestBody CourseMarket courseMarket) {
        return courseService.updateCourseMarket(id,courseMarket);
    }

    @Override   //添加图片与课程的关联关系
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam(value = "courseId") String courseId, @RequestParam(value = "pic") String pic) {
        return courseService.addCoursePic(courseId,pic);
    }

    @PreAuthorize("hasAuthority('course_pic_list')")
    @Override   //查询课程图片
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePicList(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePicList(courseId);
    }

    @Override   //删除课程图片
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePicList(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePicList(courseId);
    }

    @Override   //课程视图数据查询
    @GetMapping("/courseview/{id}")
    public CourseView courseView(@PathVariable("id") String courseId){
        return courseService.getCourseView(courseId);
    }

    @Override   //课程预览
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String courseId) {
        return courseService.preview(courseId);
    }

    @Override   //课程发布
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String courseId) {
        return courseService.publish(courseId);
    }

    @Override   //保存课程计划与媒资文件的关联
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }
}
