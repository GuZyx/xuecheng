package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

//录播课程学习管理业务层
@Service
public class LearningService {
    @Autowired
    CourseSearchClient courseSearchClient;

    @Autowired
    XcLearningCourseRepository xcLearningCourseRepository;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    //获取课程播放地址(视频播放地址)并判断是否有权限
    public GetMediaResult getMedia(String courseId, String teachplanId) {
        //校验学生的学习权限

        //远程调用搜索服务查询课程计划对应的媒资信息
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
        if(teachplanMediaPub==null|| StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())){
            //获取学习地址错误
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        return new GetMediaResult(CommonCode.SUCCESS,teachplanMediaPub.getMediaUrl());
    }

    //添加选课
    @Transactional
    public ResponseResult addCourse(String userId, String courseId, String valid, Date startTime,
                                    Date endTime, XcTask xcTask){
        if(StringUtils.isEmpty(userId)){
            //用户信息为空
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_USERISNULL);
        }
        if(StringUtils.isEmpty(courseId)){
            //课程信息为空
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_COURSEISNULL);
        }
        if(xcTask==null||StringUtils.isEmpty(xcTask.getId())){
            //任务信息为空
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_TASKISNULL);
        }

        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findByUserIdAndCourseId(userId, courseId);

        if(xcLearningCourse==null){ //没有选课记录添加
            xcLearningCourse = new XcLearningCourse();
            xcLearningCourse.setUserId(userId);
            xcLearningCourse.setCourseId(courseId);
            //课程有效期
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            //课程有效性 xcLearningCourse.setValid(valid);
            xcLearningCourse.setStatus("501001");
        }else { //有选课记录添加
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
        }
        xcLearningCourseRepository.save(xcLearningCourse);

        //向历史任务表插入数据
        Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
        if(!optional.isPresent()){
            //添加历史任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
