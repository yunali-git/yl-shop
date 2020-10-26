package com.baidu.shop.business.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OrderService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.MRshopConstant;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.OrderDTO;
import com.baidu.shop.dto.OrderInfo;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.OrderDetailEntity;
import com.baidu.shop.entity.OrderEntity;
import com.baidu.shop.entity.OrderStatusEntity;
import com.baidu.shop.mapper.OrderDetailMapper;
import com.baidu.shop.mapper.OrderMapper;
import com.baidu.shop.mapper.OrderStatusMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.IdWorker;
import com.baidu.shop.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/21
 * @Version V1.0
 **/
@RestController
public class OrderServiceImpl extends BaseApiService implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private IdWorker idWorker;

    @Resource
    private RedisRepository redisRepository;

    @Override
    public Result<OrderInfo> getOrderInfoByOrderId(Long orderId) {

        OrderEntity orderEntity = orderMapper.selectByPrimaryKey(orderId);
        OrderInfo orderInfo = BaiduBeanUtil.copyProperties(orderEntity, OrderInfo.class);

        Example example = new Example(OrderDetailEntity.class);
        example.createCriteria().andEqualTo("orderId",orderInfo.getOrderId());

        List<OrderDetailEntity> orderDetailList = orderDetailMapper.selectByExample(example);
        orderInfo.setOrderDetailList(orderDetailList);

        OrderStatusEntity orderStatusEntity = orderStatusMapper.selectByPrimaryKey(orderInfo.getOrderId());

        orderInfo.setOrderStatusEntity(orderStatusEntity);
        return this.setResultSuccess(orderInfo);
    }

    @Transactional
    @Override
    public Result<String> createOrder(OrderDTO orderDTO , String token) {

        long orderId = idWorker.nextId();
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            OrderEntity orderEntity = new OrderEntity();

            Date date = new Date();
            orderEntity.setOrderId(orderId);
            orderEntity.setUserId(userInfo.getId() + "");
            orderEntity.setSourceType(1);
            orderEntity.setInvoiceType(1);
            orderEntity.setBuyerRate(1);
            orderEntity.setBuyerMessage("谁谁呀");
            orderEntity.setBuyerNick(userInfo.getUsername());
            orderEntity.setPaymentType(orderDTO.getPayType());
            orderEntity.setCreateTime(date);

            List<Long> longs = Arrays.asList(0L);

            List<OrderDetailEntity> orderDetailList = Arrays.asList(orderDTO.getSkuIds().split(",")).stream().map(skuIdStr -> {
                Car car = redisRepository.getHash(MRshopConstant.USER_GOODS_CAR_PRE + userInfo.getId(), skuIdStr, Car.class);
                if (car == null) {
                    throw new RuntimeException("数据异常");
                }
                OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
                orderDetailEntity.setSkuId(Long.valueOf(skuIdStr));
                orderDetailEntity.setTitle(car.getTitle());
                orderDetailEntity.setPrice(car.getPrice());
                orderDetailEntity.setNum(car.getNum());
                orderDetailEntity.setImage(car.getImage());
                orderDetailEntity.setOrderId(orderId);
                longs.set(0,car.getPrice() * car.getNum() + longs.get(0));

                return orderDetailEntity;
            }).collect(Collectors.toList());

            orderEntity.setActualPay(longs.get(0));
            orderEntity.setTotalPay(longs.get(0));

            //status
            OrderStatusEntity orderStatusEntity = new OrderStatusEntity();
            orderStatusEntity.setCreateTime(date);
            orderStatusEntity.setOrderId(orderId);
            orderStatusEntity.setStatus(1);

            //入库
            orderMapper.insertSelective(orderEntity);
            orderDetailMapper.insertList(orderDetailList);
            orderStatusMapper.insertSelective(orderStatusEntity);

            //更新库存
            orderDetailList.stream().forEach(order -> {
                orderDetailMapper.update(order.getSkuId(), order.getNum());
            });

            Arrays.asList(orderDTO.getSkuIds().split(",")).stream().forEach(skuidSku -> {
                redisRepository.delHash(MRshopConstant.USER_GOODS_CAR_PRE + userInfo.getId(),skuidSku);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResult(HTTPStatus.OK,"",orderId + "");
    }
}
