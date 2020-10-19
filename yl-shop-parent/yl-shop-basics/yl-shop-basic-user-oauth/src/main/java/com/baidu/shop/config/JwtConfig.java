package com.baidu.shop.config;

import com.baidu.shop.utils.RsaUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @ClassName JwtConfig
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Data
@Configuration
public class JwtConfig {
    @Value("${mrshop.jwt.secret}")
    private String secret; // 密钥
    @Value("${mrshop.jwt.pubKeyPath}")
    private String pubKeyPath;// 公钥
    @Value("${mrshop.jwt.priKeyPath}")
    private String priKeyPath;// 私钥
    @Value("${mrshop.jwt.expire}")
    private int expire;// token有效时间
    @Value("${mrshop.jwt.cookieName}")
    private String cookieName;// cookie名称
    @Value("${mrshop.jwt.cookieMaxAge}")
    private int cookieMaxAge;// cookie有效时间

    private PublicKey publicKey; // 公钥

    private PrivateKey privateKey; // 私钥

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    //如果了解过springbean声明周期的话
    //先执行构造函数
    //接着执行属性注入
    //然后执行Post****
    //Constructor >> @Autowired >> @PostConstruct
    @PostConstruct
    public void init(){
        try {
            File pubKey = new File(pubKeyPath);
            File priKey = new File(priKeyPath);
            if (!pubKey.exists() || !priKey.exists()) {
                // 生成公钥和私钥
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            logger.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException();
        }
    }
}
