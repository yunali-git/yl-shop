package com.mr.test.SendMsg;

import com.mr.rabbitmq.RunTestRabbitMqApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName SendMsg
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/12
 * @Version V1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RunTestRabbitMqApplication.class)
public class SendMsg {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void sendMessage() throws InterruptedException {
        String message = " good good study ";
        //amqpTemplate 发送一个消息 指定：交换机名称， routingkey 参数
        amqpTemplate.convertAndSend("topic_exchange_test","x.x",message);
        System.out.println("发送成功：ok");
        // 等待10秒为了可以看到 消费者接收到消息执行
        //Thread.sleep(10000);
    }
}
