package com.mr.rabbitmq.direct;

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
    //交换机名称
    private final static String EXCHANGE_NAME = "direct_exchange_test";

    //主函数
    public static void main(String[] arg) throws Exception {
        // 获取到连接
        Connection connection = RabbitmqConnectionUtil.getConnection();
        // 获取通道
        Channel channel = connection.createChannel();

        /*
        param1: 交换机名称
        param2: 交换机类型
         */
        channel.exchangeDeclare(EXCHANGE_NAME,"direct");

        //发送的消息内容
        String message = "商品新增成功  id ： 153";
        //String message = "商品修改成功  id ： 153";
        //String message = "商品删除成功  id ： 153";
        /*
        param1: 交换机名称
        param2: routingKey
        param3: 一些配置信息
        param4: 发送的消息
         */
        //发送消息
        channel.basicPublish(EXCHANGE_NAME, "save", null, message.getBytes());
        //channel.basicPublish(EXCHANGE_NAME, "update", null, message.getBytes());
        //channel.basicPublish(EXCHANGE_NAME, "delete", null, message.getBytes());

        System.out.println(" [商品服务] 发送消息routingKey ：save   '" + message );
        // 关闭通道和连接
        channel.close();
        connection.close();
    }
}
