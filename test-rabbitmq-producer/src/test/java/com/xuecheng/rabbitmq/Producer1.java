package com.xuecheng.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 工作队列模式
 */
public class Producer1 {
    private static final String QUEUE = "hello world";
    public static void main(String[] args) {
        //通过连接工厂创建新的连接和mq建立连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        //设置虚拟机，一个mq的服务可以设置多个虚拟机，每个虚拟机相当于独立的mq
        factory.setVirtualHost("/");
        Connection connection = null;
        Channel channel = null;
        try {
            //建立新连接
            connection = factory.newConnection();
            //建立会话通道，生产者和mq服务所有的通信都在channel通道中完成
            channel = connection.createChannel();
            //声明队列  如果队列在mq当中没有，则要创建
            /*
             * 参数明细：String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
             * 1.queue 队列名称，
             * 2.durable 是否持久化，mq重启后队列还在
             * 3.exclusive 是否独占连接，队列只允许在该连接中访问
             *      true ： 如果connection连接关闭，队列自动删除(可用于临时队列的创建)
             * 4.autoDelete 队列不再使用的时候是否自动删除此队列
             *      如果将此参数和exclusive参数设置为true就可以实现临时队列
             * 5.arguments 参数，可以设置一个队列的拓展参数，
             *      eg：设置存活时间
             *
             */
            channel.queueDeclare(QUEUE,true,false,false,null);
            //发送消息
            /**
             * 参数明细：String exchange, String routingKey, BasicProperties props, byte[] body
             * 1.exchange   交换机，如果不指定将使用mq的默认交换机，设置为""
             * 2.routingKey 路由key，交换机根据路由key将消息转发到指定的队列，如果使用默认交换机，要设置为队列名称
             * 3.props  消息的属性
             * 4.body   消息内容
             */
            //定义消息内容
            String message = "HELLO World !";
            channel.basicPublish("",QUEUE,null,message.getBytes());
            System.out.println("send to mq："+message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                //先关闭通道
                assert channel != null : "通道为空";
                channel.close();
                //关闭连接
                connection.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }

    }
}
