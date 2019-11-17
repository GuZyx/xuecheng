package com.xuecheng.test.freemarker.controller;

import com.xuecheng.test.freemarker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller //这里不要使用restController，restController输出json数据
@RequestMapping("/freemarker")
public class FreemarkerController {
    @Autowired
    private RestTemplate restTemplate;

    //轮播图
    @RequestMapping("/course")
    public String course(Map<String,Object> map){
        //使用restTemplate请求轮播图的模型数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000", Map.class);
        Map body = forEntity.getBody();
        //设置模型数据
        map.putAll(body);
        return "course";
    }
    //轮播图
    @RequestMapping("/banner")
    public String banner(Map<String,Object> map){
        //使用restTemplate请求轮播图的模型数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getModel/5a791725dd573c3574ee333f", Map.class);
        Map body = forEntity.getBody();
        //设置模型数据
        map.putAll(body);
        return "index_banner";
    }

    //测试1
    @RequestMapping("/test1")
    public String test1(Map<String,Object> map){
        //map就是freemarker模板所使用的的数据
        map.put("name","传智播客 ");

        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(19);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setAge(20);
        stu2.setMoney(800.86f);
//        stu2.setBirthday(new Date());
        //朋友列表
        List<Student> friends = new ArrayList<>();
        friends.add(stu1);
        //给第二名学生设置朋友列表
        stu2.setFriends(friends);
        //给第二个学生设置最好的朋友
        stu2.setBestFriend(stu1);
        List<Student> students= new ArrayList<>();
        students.add(stu1);
        students.add(stu2);
        //将学生列表放在map中
        map.put("stus",students);
        //准备map数据
        HashMap<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        //向数据模型放数据
        map.put("stu1",stu1);
        //向数据模型放map数据
        map.put("stuMap",stuMap);

        map.put("point",121212121);
        //返回freemarker模板的位置，基于resources/templates路径
        return "test1";
    }
}

