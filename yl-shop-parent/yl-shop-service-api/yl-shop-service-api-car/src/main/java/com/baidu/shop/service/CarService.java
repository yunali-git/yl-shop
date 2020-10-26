package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.Car;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api(tags = "购物车接口")
public interface CarService {

    @ApiOperation(value = "添加商品到购物车")
    @PostMapping(value = "car/addCar")
    Result<JSONObject> addCar(@RequestBody Car car, @CookieValue("MRSHOP_TOKEN") String token);

    @ApiOperation(value = "购物车合并")
    @PostMapping(value = "car/mergeCar")
    Result<JSONObject> mergeCar(@RequestBody String clientCarList, @CookieValue(value = "MRSHOP_TOKEN") String token);

    @ApiOperation(value="获取当前用户购物车数据")
    @GetMapping(value = "car/getUserGoodsCar")
    Result<List<Car>> getUserGoodsCar(@CookieValue(value = "MRSHOP_TOKEN") String token);

    @ApiOperation(value = "修改商品在购物车中的数量")
    @GetMapping(value = "car/carNumUpdate")
    Result<JSONObject> carNumUpdate(Long skuId ,Integer type ,@CookieValue(value = "MRSHOP_TOKEN") String token);
}

