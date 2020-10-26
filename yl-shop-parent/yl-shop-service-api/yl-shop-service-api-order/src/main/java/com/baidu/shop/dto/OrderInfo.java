package com.baidu.shop.dto;

import com.baidu.shop.entity.OrderDetailEntity;
import com.baidu.shop.entity.OrderStatusEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName OrderInfo
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/22
 * @Version V1.0
 **/
@Data
public class OrderInfo {

    private Long orderId;//订单id

    private Long totalPay;//总金额,单位为分

    private Long actualPay;//实付金额,有活动或者优惠的话可能会与实际金额不一直

    private String promotionIds;//促销/活动的id集合

    private Integer paymentType;//支付类型

    private Date createTime;//订单生成时间

    private String userId;//用户id,可以换成与用户表一直的数据类型

    private String buyerMessage;//买家留言

    private String buyerNick;

    private Integer buyerRate;//买家是否已经评价

    private Integer invoiceType;//发票类型;

    private Integer sourceType;//订单来源

    private List<OrderDetailEntity> orderDetailList;

    private OrderStatusEntity orderStatusEntity;
}

