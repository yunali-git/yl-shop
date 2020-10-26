package com.baidu.shop.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @ClassName AlipayConfig
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/22
 * @Version V1.0
 **/
public class AlipayConfig {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016102600766474";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCAJ0h7x82FTqiYZvwDTljnidpVTA1MWxq4yBtkNYB2HqpxdDB2tvG89YfORr4CWbkJSVCfJIt4brFOI5e3rAT7OPUi6Q/fLo9cHuA0a1afbCMInOrQpDwyNkji7TZ23m5EN/QenEfAmig4x/F86R/2nIXfnViu+s8cTJn+X0Wn6nv2JKqDhUMXIfMGgRNMJ8gUgqsl1I78itnUxUvM0jsW4p6StdiHDmIBnby46KIE2Ww1Im1NKpYc/A+D2DkLlgWF00CQeiYn5vUXT6BDVDEcrRM5KAIMjcZJWZ61f5HUv+nixzOMiLqhPWdSheAuu0CzodpqPDdF5z1wj1crQ8T1AgMBAAECggEAaluv7YMSj9uLUwjNiRTh9i/Ts0zuEHvf/pGdIhrBJccnoAdVHYGRuUhxUIInuh2q0wk0zw9ubSI5JuelR/uNGs7XrASPdqPWuRaq7vZSqvb9UG2i+EwF5YpmDWff9uF91EI36Zu8dR272fwe8JMuQub+uGqHKpnff9YXTCTX05Y79LvtC7WwL723vb4qEPgN+7vnDea/A3mQwlie706W5XmEAha+sYOgZTtunQGO3O1xc/9gjarG3/Vk86uM+EY80znFbefuNlm+MmiXfY8vxFc41+c0aWwG90gXoCOhsbHo1Fy6ZnAWWatccK4+R/oRP1Xd3+EXt8SFccMimjfwoQKBgQC/b1tjajig1qcWxYQRuTCsZJKEZks0Oqt4XWN84c+kjT4PM2g7WhSoFUxg8I6jytpdTnEdEJOMkyk3qCQlWBsVy2ohRyKGbCV9TES7Rq8E2nVIu1vfIBYdohoKCkYNHYkt5Fo2pAvoUhv8fs9DsycTG/V83mqTcM7SYWmPfW3mCQKBgQCrYCZaXWpXvRupQn18GhozhKDAQI1AhXbt7XmgmtRlWk5U7URDSpaaumBRKFo9ChOMmZC8bHz1teBuhw1ARPBITisyw6EJs9uw3YptOHHiA2hYEmaNEyUKfAlGlZFwi80qiumIMQxCDRQgCuyH7Emfn6G5AKNtWnxZWfPYZnACjQKBgQCtNHPbXyAdZhmH4O29m3EOiXCHmoYwm2TJYrXBKA5XM/QSjNCh4r8N1Tdl/kEQsVkhUvfE9FqLthp04QzTPQREk+dSdKooNXE2DOujBjEUkxPK2rolB0L7TM+ChgkblL2paBK3nXWonaH0skeVWWLhR0q10Addn5OBheGckj/9AQKBgFahZyHdAQnTPa1lQBvA8ifCzqQAvaa1EPabShWGzwFhEiHaQtylUtZqio/cDym180XqxwCI8u4ef5I/0NIBR1m1sh6aR8io09BnXTdzBhaTVGbY/mAoIlxXcTTuHLRA53iu+fhvmtu00syT2ReouVrojg8AJJz91cgxMZPdzJdtAoGANYoaIw0XCK5GaskQL6jJyrosFjlv43InXvd0uTmQPaNpXbIfSSQakmtc1JjClcLtCQnHSeRShqGTet0+EkhCeSjaH24iYW10MnfAQwrCWvg/E7U4D86TAdKBDG7ZqJFJgmidVMheek6AEzmlUbvanRCR+vEjY960Tm/N8xrgtUM=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0NI3dal+VmU1x5iwnQZ/TbGhCacstYTh/1XYKyRHRM/fsKgF9MlMmQVKAiBJBJyV79hqp04Kaz2JyrYgacAlqjK+87KTBTRBWfjcopOmiaO9gG0IbzmrG6lI7xzL3meb9nxYsmFTVhGtgSPZNJrNJzeX6mko4nqx82dmN3tVg4sD4Rf80Rd+JiOTllooxHWgICAzA28Lk5Qiy1tx9M8UYYh8bdPcfVXr3ha3xzzwlI8FyF1R7xZq88SZMfuJsw5IFt4+1cKYz/dDdG9pTXqh/nkrzjvw1TjngHNBEQOGAy9BVSL6iMEBjBLaGEVd4YFGX8BzBCn0QKLFzHrXVu6PCQIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8900/pay/returnNotify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://localhost:8900/pay/returnURL";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "D:\\";

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

