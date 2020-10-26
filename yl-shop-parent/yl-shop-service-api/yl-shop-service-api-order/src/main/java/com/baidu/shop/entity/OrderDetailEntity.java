package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName OrderDetailEntity
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/21
 * @Version V1.0
 **/
@Table(name = "tb_order_detail")
@Data
public class OrderDetailEntity {

    @Id
    private Long id;//订单详情id

    private Long orderId;//订单id

    private Long skuId;//商品id

    private Integer num;//购买数量

    private String title;//商品标题

    private String ownSpec;//商品特有属性值

    private Long price;//商品价格

    private String image;//商品图片

}
