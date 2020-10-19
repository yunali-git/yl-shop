package com.baidu.filter;

import com.baidu.config.JwtConfig;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName LoginFilter
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/17
 * @Version V1.0
 **/
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtConfig jwtConfig;

    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest req = ctx.getRequest();
        // 获取请求的url
        String requestURI = req.getRequestURI();
        logger.debug("=============" + requestURI);
        //如果当前请求是登录请求,不执行拦截器
        return !jwtConfig.getExcludePath().contains(requestURI);
    }

    @Override
    public Object run() throws ZuulException {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest request = ctx.getRequest();
        logger.info("拦截到请求"+request.getRequestURI());
        // 获取token
        String token = CookieUtils.getCookieValue(request, jwtConfig.getCookieName());
        logger.info("token信息"+token);

        if(token != null){
            // 校验
            try {
                // 通过公钥解密，如果成功，就放行，失败就拦截
                JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            } catch (Exception e) {
                logger.info("解析失败 拦截"+token);
                // 校验出现异常，返回403
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
            }
        }else {
            logger.info("没有token");
            // 校验出现异常，返回403
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
        }

        return null;
    }
}

