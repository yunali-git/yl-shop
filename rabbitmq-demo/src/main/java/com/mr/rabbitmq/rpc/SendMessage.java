package com.mr.rabbitmq.rpc;

import com.mr.rabbitmq.utils.RabbitmqConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

/**
 * @ClassName SendMessage
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/10
 * @Version V1.0
 **/
public class SendMessage {
    //交换机名称
    private final static String EXCHANGE_NAME = "exchange_pub";

    public static void main(String[] args) throws Exception{

        Connection connection = RabbitmqConnectionUtil.getConnection();

        Channel channel = connection.createChannel();

        //是当前的channel处于确认模式
        channel.confirmSelect();

        // 声明创建交换机exchange，指定类型为fanout
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        //消息内容
        String message="good good study";
        channel.basicPublish(EXCHANGE_NAME,"save",
                MessageProperties.PERSISTENT_BASIC,message.getBytes());
        //确认消息，发送完毕
        if(channel.waitForConfirms()){
            System.out.println("发送成功");
        }else{
            System.out.println("发送失败");
        }

        channel.close();
        connection.close();
    }
}
