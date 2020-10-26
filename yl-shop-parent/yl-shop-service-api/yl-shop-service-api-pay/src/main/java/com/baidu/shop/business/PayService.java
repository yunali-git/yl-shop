package com.baidu.shop.business;

import com.baidu.shop.dto.PayInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "支付接口")
public interface PayService {
    @ApiOperation(value = "请求支付")
    @GetMapping(value = "pay/requestPay")
    void requestPay(PayInfoDTO payInfoDTO, HttpServletResponse httpServletResponse);

    @ApiOperation(value = "接受支付宝通知")
    @GetMapping(value = "pay/returnNotify")
    void returnNotify(HttpServletRequest httpServletRequest);

    @ApiOperation(value = "返回支付成功页面")
    @GetMapping(value = "pay/returnURL")
    void returnURL(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse);
}
