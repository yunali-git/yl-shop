package com.baidu.shop.config;

import com.baidu.shop.utils.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName IdWorkerConfig
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/21
 * @Version V1.0
 **/
@Configuration
public class IdWorkerConfig {

    @Value(value = "${mrshop.worker.workerId}")
    private long workerId;// 当前机器id

    @Value(value = "${mrshop.worker.datacenterId}")
    private long datacenterId;// 序列号

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(workerId, datacenterId);
    }
}
