package com.baidu.shop.mapper;

import com.baidu.shop.entity.BrandEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<BrandEntity> {

    @Select(value = "select * from tb_brand b where b.id in(select cb.brand_id from tb_category_brand cb where cb.category_id=#{cid})")
    List<BrandEntity> getBrandByCategory(Integer cid);
}
