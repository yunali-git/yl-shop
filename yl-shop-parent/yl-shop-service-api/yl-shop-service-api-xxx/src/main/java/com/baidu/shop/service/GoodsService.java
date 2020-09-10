package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "查询spu信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO);

    @ApiOperation(value = "新增spu信息")
    @PostMapping(value = "goods/spuAdd")
    Result<JsonObject> spuAdd(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "查询spu信息")
    @GetMapping(value = "goods/getSpuDetailBydSpu")
    Result<SpuDetailEntity> getSpuDetailBydSpu (Integer spuId);

    @ApiOperation(value = "获取sku信息")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(Integer spuId);

    @ApiOperation(value = "修改spu信息")
    @PutMapping(value = "goods/spuAdd")
    Result<JsonObject> spuEdit(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "修改spu信息")
    @DeleteMapping(value = "goods/spuDelete")
    Result<JsonObject> spuDelete(Integer spuId);

    @ApiOperation(value = "修改上下架的状态")
    @PutMapping(value = "goods/upOrDown")
    Result<JsonObject> upOrDownEdit(@RequestBody SpuDTO spuDTO);

}
