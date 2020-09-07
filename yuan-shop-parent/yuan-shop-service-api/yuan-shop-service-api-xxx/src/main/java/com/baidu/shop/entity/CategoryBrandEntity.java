package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Table;

/**
 * @ClassName CategoryBrandEntity
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/2
 * @Version V1.0
 **/
@Table(name = "tb_category_brand")
@Data
public class CategoryBrandEntity {
    private Integer categoryId;

    private Integer brandId;

}
