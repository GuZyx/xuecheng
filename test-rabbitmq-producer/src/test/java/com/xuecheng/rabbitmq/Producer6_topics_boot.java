package com.xuecheng.rabbitmq;

import com.xuecheng.test.rabbitmq.TestRabbitmqApplicationProducer;
import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 通配符模式生产者
 */
@SpringBootTest(classes = TestRabbitmqApplicationProducer.class)
@RunWith(SpringRunner.class)
public class Producer6_topics_boot {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //使用rabbitTemplate发送消息
    @Test
    public void testSendEmail(){
        /**
         * 参数：String exchange, String routingKey, Object object
         * 1.交换机名称
         * 2.routingKey
         * 3.消息内容
         */
        String message = "send email to user";
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM,"inform.email",message);
    }
}
