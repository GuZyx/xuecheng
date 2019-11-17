package com.xuecheng.search.controller;

import com.xuecheng.api.search.ESCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.ESCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ES搜索视图层
 */
@RestController
@RequestMapping("/search/course")
public class ESCourseController implements ESCourseControllerApi {

    @Autowired
    private ESCourseService esCourseService;

    @Override   //搜索课程信息
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(@PathVariable("page") int page, @PathVariable("size") int size, CourseSearchParam courseSearchParam) {
        return esCourseService.list(page,size,courseSearchParam);
    }

    @Override   //根据课程id查询课程信息
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getall(@PathVariable("id") String id) {
        return esCourseService.getall(id);
    }

    @Override   //根据多个课程计划id查询课程媒资信息
    @GetMapping("/getmedia/{teachplanId}")
    public TeachplanMediaPub getmedia(@PathVariable("teachplanId") String id) {
        //将一个id加入数组
        String[] teachplanIds = new String[]{id};
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = esCourseService.getmedia(teachplanIds);
        QueryResult<TeachplanMediaPub> queryResult = queryResponseResult.getQueryResult();
        if(queryResult!=null){
            List<TeachplanMediaPub> list = queryResult.getList();
            if(list!=null && list.size()>0){
                return list.get(0);
            }
        }
        return new TeachplanMediaPub();
    }
}
