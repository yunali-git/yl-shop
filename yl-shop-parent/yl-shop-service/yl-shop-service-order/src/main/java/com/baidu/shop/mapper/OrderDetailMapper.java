package com.baidu.shop.mapper;

import com.baidu.shop.entity.OrderDetailEntity;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface OrderDetailMapper extends Mapper<OrderDetailEntity>, InsertListMapper<OrderDetailEntity> {
    @Update(value = "update tb_stock t set t.stock = (\n" +
            "\t ( select * from ( select stock from tb_stock where sku_id = #{skuId} ) a ) - #{num} \n" +
            ") \n" +
            "where t.sku_id =  #{skuId}")
    void update (Long skuId, Integer num);

}
