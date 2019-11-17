package com.xuecheng.test.rabbitmq.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 *  接收消息类_topic
 */
@Component
public class ReceiveHandler_topic {

    @RabbitListener(queues ={RabbitmqConfig.QUEUE_INFORM_EMAIL} )
    public void email_send(String msg,Message message,Channel channel){
        System.out.println("receive message is :"+msg);
    }
}
