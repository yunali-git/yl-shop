package com.baidu.shop.business.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.PayService;
import com.baidu.shop.config.AlipayConfig;
import com.baidu.shop.dto.OrderInfo;
import com.baidu.shop.dto.PayInfoDTO;
import com.baidu.shop.feign.OrderFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PayServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/22
 * @Version V1.0
 **/
@Controller
public class PayServiceImpl extends BaseApiService implements PayService {
    @Autowired
    private OrderFeign orderFeign;

    @Override
    public void returnURL(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        //获取支付宝GET过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = httpServletRequest.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            try {
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            params.put(name, valueStr);
        }

        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);


        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //——请在这里编写您的程序（以下代码仅作参考）——
        if(signVerified) {
            //商户订单号
            try {
                String out_trade_no = new String(httpServletRequest.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
                //支付宝交易号
                String trade_no = new String(httpServletRequest.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

                //付款金额
                String total_amount = new String(httpServletRequest.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

                //修改订单状态
                try {
                    httpServletResponse.sendRedirect("http://www.mrshop.com/success.html?orderId=" + out_trade_no + "&totalPay=" + total_amount);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //out.println("trade_no:"+trade_no+"<br/>out_trade_no:"+out_trade_no+"<br/>total_amount:"+total_amount);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else {
            //out.println("验签失败");
            //打印错误日志
            //返回错误页面
        }
    }

    @Override
    public void returnNotify(HttpServletRequest httpServletRequest) {
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = httpServletRequest.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            try {
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            params.put(name, valueStr);
        }

        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //——请在这里编写您的程序（以下代码仅作参考）——

        /* 实际验证过程建议商户务必添加以下校验：
        1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
        4、验证app_id是否为该商户本身。
        */
        if(signVerified) {//验证成功
            //商户订单号
            String trade_status = "";
            try {
                String out_trade_no = new String(httpServletRequest.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

                //支付宝交易号
                String trade_no = new String(httpServletRequest.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

                //交易状态
                trade_status = new String(httpServletRequest.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(trade_status.equals("TRADE_FINISHED")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
            }else if (trade_status.equals("TRADE_SUCCESS")){
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //如果有做过处理，不执行商户的业务程序

                    //注意：
                    //付款完成后，支付宝系统发送该交易状态通知
                }

//                out.println("success");

            }else {//验证失败
//                out.println("fail");

                //调试用，写文本函数记录程序运行情况是否正常
                //String sWord = AlipaySignature.getSignCheckContentV1(params);
                //AlipayConfig.logResult(sWord);
            }
    }

    @Override
    public void requestPay(PayInfoDTO payInfoDTO , HttpServletResponse httpServletResponse) {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        Result<OrderInfo> orderResult = orderFeign.getOrderInfoByOrderId(payInfoDTO.getOrderId());
        if(orderResult.getCode() == 200){
            OrderInfo orderInfo = orderResult.getData();
            List<String> titleList = orderInfo.getOrderDetailList().stream().map(orderDetail -> orderDetail.getTitle()).collect(Collectors.toList());

            String titleStr = String.join(",", titleList);
            titleStr = titleStr.length() > 10 ? titleStr.substring(0,10) : titleStr;

            //商户订单号 必填
            String out_trade_no = payInfoDTO.getOrderId() + "";
            //付款金额 必填
            String total_amount = Double.valueOf(orderInfo.getActualPay()) / 100 + "";
            //订单名称 必填
            String subject = titleStr;
            //商品描述 可空
            String body = "";

            alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                    + "\"total_amount\":\""+ total_amount +"\","
                    + "\"subject\":\""+ subject +"\","
                    + "\"body\":\""+ body +"\","
                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

            //请求
            String result = null;
            try {
                result = alipayClient.pageExecute(alipayRequest).getBody();
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            new HashMap<>();
            //输出
            httpServletResponse.setContentType("text/html; charset=utf-8");
            PrintWriter out = null;
            try {
                out = httpServletResponse.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.println(result);
        }
    }
}
