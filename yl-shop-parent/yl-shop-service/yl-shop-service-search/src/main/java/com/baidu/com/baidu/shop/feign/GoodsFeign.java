package com.baidu.com.baidu.shop.feign;

import com.baidu.shop.service.GoodsService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName GoodsFeign
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/16
 * @Version V1.0
 **/
@FeignClient(contextId = "GoodsService", value = "xxx-service")
public interface GoodsFeign extends GoodsService {
}
