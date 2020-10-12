package com.mr.rabbitmq.rpc;

import com.mr.rabbitmq.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @ClassName RpcSendMessage
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/10
 * @Version V1.0
 **/
public class RpcSendMessage {
    //交换机名称
    private final static String EXCHANGE_NAME = "exchange_rpc";
    //队列名称
    private final static String QUEUE_NAME = "queue_rpc";

    public static void main(String[] args)throws Exception {
        // 获取连接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        // 创建通道
        final Channel channel = connection.createChannel();
        //创建交换机--先删除后增加
        channel.exchangeDelete(EXCHANGE_NAME);
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", false, false, null);

        //创建队列
        channel.queueDelete(QUEUE_NAME);
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //绑定队列交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "rpc");

        //此处注意：我们声明了要回复的队列。队列名称由RabbitMQ自动创建。
        //这样做的好处是：每个客户端有属于自己的唯一回复队列，生命周期同客户端
        String replyQueue = channel.queueDeclare().getQueue();

        final String corrID ="9527";

        //消息 指定回复队列和ID

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        // 指定回复队列和回复correlateId
        builder.replyTo(replyQueue).correlationId(corrID);
        AMQP.BasicProperties properties = builder.build();

        // 消息参数内容
        String message = "good good study ";

        System.out.println("rpc模型消息发送  "+message);
        channel.basicPublish(EXCHANGE_NAME, "rpc", properties,
                message.getBytes());

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 监听队列中的消息，如果有消息，进行处理
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                if (corrID.equals(properties.getCorrelationId())) {
                    System.out.println("9527  ID对应上的消息：" + new String(body));
                } else {
                    System.out.println("9527  ID未对应上的消息：" + new String(body));
                }
            }
        };
        channel.basicConsume(replyQueue, true, consumer);
    }
}
