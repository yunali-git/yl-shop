package com.mr.rabbitmq.work;

import com.mr.rabbitmq.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @ClassName ReceiveOne
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/9
 * @Version V1.0
 **/
public class ReceiveOne {
    //队列名称
    private final static String QUEUE_NAME = "test_work_queue";

    public static void main(String[] arg) throws Exception {
        // 获取连接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        // 创建通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //道设置 一个消费者获取一条消息，执行完毕，再获取下一条
        channel.basicQos(1);

        // 定义队列 接收端==》消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 监听队列中的消息，如果有消息，进行处理
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body： 消息中参数信息
                String msg = new String(body);
                System.out.println(" [消费者-1] 收到消息 : " + msg );
                //System.out.println(1/0);
                /*
                param1 : （唯一标识 ID）
                param2 : 是否进行批处理
                 */
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
       /*
       param1 : 队列名称
       param2 : 是否自动确认消息
       param3 : 消费者
        */
        channel.basicConsume(QUEUE_NAME, false, consumer);

        //消费者需要时时监听消息，不用关闭通道与连接
    }
}
