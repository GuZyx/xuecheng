package com.xuecheng.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 入门程序消费者
 */
public class Consumer1 {
    private static final String QUEUE = "hello world";

    public static void main(String[] args) throws IOException, TimeoutException {
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
        //建立新链接
        connection=factory.newConnection();
        //建立通道
        channel=connection.createChannel();

        //声明队列  如果队列在mq当中没有，则要创建
        /**
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

        //实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
            /**
             * @param consumerTag 消费者标签  用来表示消费者，在监听队列的时候设置channel.basicConsume
             * @param envelope 信封，通过envelope获取
             * @param properties 消息属性 channel.basicPublish中的BasicProperties props
             * @param body 消息内容
             * @throws IOException
             */
            @Override   //当接收到消息后此方法被调用
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //获取交换机
                String exchange = envelope.getExchange();
                //获取消息id，mq在同道中用来标识的消息id,可用于确认消息已接受
                long deliveryTag = envelope.getDeliveryTag();
                //获取路由key
                String routingKey = envelope.getRoutingKey();
                //消息内容
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("receive message："+message);
                super.handleDelivery(consumerTag, envelope, properties, body);
            }
        };

        //监听队列
        /**
         * 参数明细：String queue, boolean autoAck, Consumer callback
         * 1.queue  队列名称
         * 2.autoAck 是否自动回复，当消费者接通到消息后要告诉mq消息已接收
         *      true：表示自动回复
         *      false：通过编程实现回复
         * 3.callback 消费方法，当消费者接收到消息要执行的方法
         */
        channel.basicConsume(QUEUE,true,defaultConsumer);

    }
}
