package com.xuecheng.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 发布订阅模式
 */
public class Producer2_publish {
    private static final String QUEUE_INFORM_EMAIL="queue_inform_email";
    private static final String QUEUE_INFORM_SMS="queue_inform_sms";
    private static final String EXCHANGE_FANOUT_INFORM="exchange_fanout_inform";
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
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);

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
            channel.queueDeclare(QUEUE_INFORM_EMAIL,true,false,false,null);
            channel.queueDeclare(QUEUE_INFORM_SMS,true,false,false,null);

            //交换机和队列进行绑定
            /**
             * 参数明细：String queue, String exchange, String routingKey
             * 1.queue  队列名称
             * 2.exchange 交换机名称
             * 3.routingKey 路由key，在发布订阅模式中设置为空串
             *      作用：交换机根据路由key的值将消息转发到指定的队列中
             */
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_FANOUT_INFORM,"");
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_FANOUT_INFORM,"");

            //发送消息
            /**
             * 参数明细：String exchange, String routingKey, BasicProperties props, byte[] body
             * 1.exchange   交换机，如果不指定将使用mq的默认交换机，设置为""
             * 2.routingKey 路由key，交换机根据路由key将消息转发到指定的队列，如果使用默认交换机，要设置为队列名称
             * 3.props  消息的属性
             * 4.body   消息内容
             */

            //定义消息内容
            for(int i=0;i<5;i++){
                String message = "send inform message to user ";
                channel.basicPublish(EXCHANGE_FANOUT_INFORM,"",null,message.getBytes());
                System.out.println("send to mq："+message);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                //先关闭通道
                assert channel != null : "通道为空";
                channel.close();
                //关闭连接
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
