package com.mr.rabbitmq.work;

import com.mr.rabbitmq.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @ClassName SendMessage
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/9
 * @Version V1.0
 **/
public class SendMessage {
    //序列名称
    private final static String QUEUE_NAME = "test_work_queue";

    //主函数
    public static void main(String[] arg) throws Exception {
        // 获取到连接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        // 获取通道
        Channel channel = connection.createChannel();

        /*
        param1:队列名称
        param2: 是否持久化
        param3: 是否排外
        param4: 是否自动删除
        param5: 其他参数
         */
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 循环发送消息100条
        for (int i = 0; i < 100; i++) {
            // 消息参数内容
            String message = "task - good study -" + i;

            /*
            param1: 交换机名称
            param2: routingKey
            param3: 一些配置信息
            param4: 发送的消息
             */
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

            System.out.println(" send '" + message + "' success");

        }

        // 关闭通道和连接
        channel.close();
        connection.close();
    }
}
