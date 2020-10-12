package com.mr.rabbitmq.rpc;

import com.mr.rabbitmq.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @ClassName RpcReceive
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/10
 * @Version V1.0
 **/
public class RpcReceive {
    //队列名称
    private final static String QUEUE_NAME = "queue_rpc";

    public static void main(String[] args) throws Exception {
        // 获取连接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        // 创建通道
        final Channel channel = connection.createChannel();

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 监听队列中的消息，如果有消息，进行处理
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                System.out.println("收到消息 执行中："+new String(body));

                AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
                //我们在将要回复的消息属性中，放入从客户端传递过来的correlateId 关系id
                builder.correlationId(properties.getCorrelationId());
                AMQP.BasicProperties prop = builder.build();

                //发送给回复队列的消息，exchange=""，routingKey=回复队列名称
                //因为RabbitMQ对于队列，始终存在一个默认exchange=""，routingKey=队列名称的绑定关系
                String message= new String(body) + "-收到 over 一定 study";
                channel.basicPublish("", properties.getReplyTo(), prop,message.getBytes());
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
