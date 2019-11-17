package com.xuecheng.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 路由模式_消费者_sms
 */
public class Consumer3_routing_sms {
    private static final String QUEUE_INFORM_SMS="queue_inform_sms";
    private static final String EXCHANGE_ROUTING_INFORM="exchange_routing_inform";
    private static final String ROUTING_SMS="inform sms";

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
        channel.queueDeclare(QUEUE_INFORM_SMS,true,false,false,null);

        //声明交换机
        /**
         * 参数明细：String exchange, String type
         * 1.exchange 交换机名称
         * 2.type 交换机类型
         *      fanout: 对应的rabbitmq的工作模式是 publish/subscribe(发布/订阅)
         *      direct: 对应的routing的工作模式(路由)
         *      topic:  对应的topic的工作模式(通配符)
         *      headers:对应的header的工作模式
         */
        channel.exchangeDeclare(EXCHANGE_ROUTING_INFORM, BuiltinExchangeType.DIRECT);

        //交换机和队列进行绑定
        /**
         * 参数明细：String queue, String exchange, String routingKey
         * 1.queue  队列名称
         * 2.exchange 交换机名称
         * 3.routingKey 路由key，在发布订阅模式中设置为空串
         *      作用：交换机根据路由key的值将消息转发到指定的队列中
         */
        channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_ROUTING_INFORM,ROUTING_SMS);

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
        channel.basicConsume(QUEUE_INFORM_SMS,true,defaultConsumer);

    }
}
