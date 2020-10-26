package com.baidu.shop.business;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.OrderDTO;
import com.baidu.shop.dto.OrderInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "订单接口")
public interface OrderService {

    @ApiOperation(value = "创建订单")
    @PostMapping(value = "order/createOrder")
    Result<String> createOrder(@RequestBody OrderDTO orderDTO, @CookieValue(value = "MRSHOP_TOKEN") String token);

    @ApiModelProperty(value = "根据订单id查询订单信息")
    @GetMapping(value = "order/getOrderInfoByOrderId")
    Result<OrderInfo> getOrderInfoByOrderId(@RequestParam Long orderId);

}