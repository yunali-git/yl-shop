package com.baidu.shop.feign;

import com.baidu.shop.service.BrandService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName BrandFeign
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/21
 * @Version V1.0
 **/
@FeignClient(value = "xxx-service",contextId = "BrandService")
public interface BrandFeign extends BrandService {

}
