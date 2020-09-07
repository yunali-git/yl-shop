package com.baidu.shop.utils;

/**
 * @ClassName StringUtil
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/2
 * @Version V1.0
 **/
public class StringUtil {
    //判断字符串类型不为空且不为null
    public static Boolean isNotEmpty(String str){
        return null != str && !"".equals(str);
    }

    //判断字符串类型为空或为null
    public static Boolean isEmpty(String str){
        return null == str || "".equals(str);
    }

    //将string类型的数据转换成Integer类型
    public static Integer toInteger(String str){
        if(isNotEmpty(str)) return Integer.parseInt(str);
        return 0;
    }
}
