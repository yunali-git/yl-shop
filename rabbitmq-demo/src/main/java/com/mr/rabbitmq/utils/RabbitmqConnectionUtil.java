package com.mr.rabbitmq.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @ClassName RabbitmqConnectionUtil
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/9
 * @Version V1.0
 **/
public class RabbitmqConnectionUtil {
    public static Connection getConnection() throws Exception {
        //定义rabbitmq连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置超时时间
        factory.setConnectionTimeout(60000);
        //设置服务ip
        factory.setHost("127.0.0.1");
        //设置端口5672
        factory.setPort(5672);
        //设置，用户名、密码、虚拟主机
        factory.setUsername("guest");
        factory.setPassword("guest");
        // 创建连接，根据工厂
        Connection connection = factory.newConnection();
        return connection;
    }
}
