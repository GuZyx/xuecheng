package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class ChooseCourseTask {
    @Autowired
    private LearningService learningService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChooseCourseTask(XcTask xcTask) {
        try {
            //取出消息内容
            String requestBody = xcTask.getRequestBody();
            Map map = JSON.parseObject(requestBody, Map.class);
            String userId = (String) map.get("userId");
            String courseId = (String) map.get("courseId");
            Date startTime = null;
            Date endTime = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

            //解析出String valid, Date startTime,Date endTime...
            //String valid = (String) map.get("valid");
            if(map.get("startTime")!=null){
                startTime = dateFormat.parse((String) map.get("startTime"));
            }
            if(map.get("endTime")!=null){
                startTime = dateFormat.parse((String) map.get("endTime"));
            }

            //添加选课
            //String userId, String courseId, String valid, Date startTime,Date endTime, XcTask xcTask
            ResponseResult result = learningService.addCourse(userId, courseId, null, startTime, endTime, xcTask);
            if(result.isSuccess()){
                //添加选课成功，要向mq发送完成添加选课信息
                rabbitTemplate.convertAndSend(xcTask.getMqExchange(),
                        RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,xcTask);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
