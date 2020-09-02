package com.baidu.shop.utils;

/**
 * @ClassName ObjectUtil
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/8/31
 * @Version V1.0
 **/
public class ObjectUtil {

    public static Boolean isNull(Object obj){
        return null == obj;
    }

    public static Boolean isNotNull(Object obj){
        return null != obj;
    }
}
