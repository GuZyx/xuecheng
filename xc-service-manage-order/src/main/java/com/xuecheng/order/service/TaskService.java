package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    private XcTaskRepository xcTaskRepository;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //查询前n条任务
    public List<XcTask> findXcTaskList(Date updateTime,int size){
        //设置分页参数
        Pageable pageable = new PageRequest(0,size);

        Page<XcTask> page = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        //查询前n条任务
        return page.getContent();
    }

    //发布消息
    @Transactional
    public void publish(XcTask xcTask,String ex,String routingKey){
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if(optional.isPresent()){
            //发送消息
            rabbitTemplate.convertAndSend(ex,routingKey,xcTask);
            //更新任务时间
//            XcTask one = optional.get();
//            one.setUpdateTime(new Date());
//            xcTaskRepository.save(one);
            int i = xcTaskRepository.updateTaskTime(xcTask.getId(), new Date());
        }
    }

    //获取任务
    @Transactional
    public int getTask(String id,int version){
        //通过乐观锁的方式来更新数据库表，如果结果大于0，取到任务
        return xcTaskRepository.updateTaskVersion(id, version);
    }

    //完成任务
    @Transactional
    public void finishTask(XcTask xcTask){
        //历史任务
        XcTaskHis xcTaskHis = new XcTaskHis();
        BeanUtils.copyProperties(xcTask,xcTaskHis);
        //把历史任务保存
        xcTaskHisRepository.save(xcTaskHis);
        //把当前任务删除
        xcTaskRepository.delete(xcTask);
    }
}
