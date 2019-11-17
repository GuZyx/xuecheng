package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * spring task定时任务
 */
@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    private TaskService taskService;

    //监听完成添加课程队列
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishAddChooseCourse(XcTask xcTask){
        if(xcTask!=null&& StringUtils.isNotEmpty(xcTask.getId())){
            LOGGER.info("receiveFinishAddChooseCourse...{}"+xcTask.getId());
            //删除任务，添加历史任务
            taskService.finishTask(xcTask);
        }
    }

    //定时发送添加选课任务
//    @Scheduled(cron = "0/5 * * * * *")
    @Scheduled(fixedDelay = 60000)
    public void sendChooseTask(){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> xcTaskList = taskService.findXcTaskList(time, 100);
//        System.out.println(xcTaskList);
        //调用service发送消息，将添加选课的任务发送给mq
        for(XcTask xcTask:xcTaskList){
            //取任务
            if(taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                //要发送的交换机
                String ex = xcTask.getMqExchange();
                //要发送的routingKey
                String rountingKey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,ex,rountingKey);
            }
        }
    }



    //定义任务调度的策略
//    @Scheduled(cron = "0/5 * * * * *")    //每隔5秒任务调度一次
//    @Scheduled(initialDelay = 3000,fixedRate = 5000)    //第一次延迟3秒，之后5秒
//    @Scheduled(fixedDelay = 3000)    //在任务执行完毕3秒后执行
//    @Scheduled(fixedRate = 3000)    //在任务开始3秒后执行下一次调度，不管任务有没有执行完
    public void test1(){
        LOGGER.info("======================任务1开始执行======================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("======================任务1执行结束======================");
    }

    //定义任务调度的策略
//    @Scheduled(cron = "0/5 * * * * *")    //每隔5秒任务调度一次
//    @Scheduled(fixedDelay = 3000)    //在任务执行完毕3秒后执行
//    @Scheduled(initialDelay = 3000,fixedRate = 5000)    //第一次延迟3秒，之后5秒
//    @Scheduled(fixedRate = 3000)    //在任务开始3秒后执行下一次调度，不管任务有没有执行完
    public void test2(){
        LOGGER.info("======================任务2开始执行======================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("======================任务2执行结束======================");
    }
}
