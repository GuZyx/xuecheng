package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.TeachplanMapper;
import com.xuecheng.manage_course.dao.TeachplanRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 课程计划业务层
 */
@Service
@Transactional
public class TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseService courseService;

    //课程计划的查询
    public TeachplanNode findTeachPlanList(String courseId){
        return teachplanMapper.selectList(courseId);
    }

    //添加课程计划
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if(teachplan==null || StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划id
        String courseid = teachplan.getCourseid();
        //parentid
        String parentid = teachplan.getParentid();
        //要处理parentId
        if(StringUtils.isEmpty(parentid)){
            parentid = this.getTeachplanRoot(courseid);
        }
        //父节点
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        String grade = parentNode.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachplan信息拷贝到teachplanNew对象中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if(grade.equals("1")){
            teachplanNew.setGrade("2"); //级别,根据父节点的级别来设置
        }else {
            teachplanNew.setGrade("3"); //级别,根据父节点的级别来设置
        }
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程的根节点，查询不到自动添加一个根节点
    private String getTeachplanRoot(String courseId){
        //课程信息
        CourseBase courseBase = courseService.getCourseBaseById(courseId);
        //查询课程的根节点
        List<Teachplan> teachplans = teachplanRepository.findByCourseidAndAndParentid(courseId, "0");
        if(teachplans==null || teachplans.size() <= 0){
            //查询不到，要自动添加根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根节点的id
        return teachplans.get(0).getId();
    }
}
