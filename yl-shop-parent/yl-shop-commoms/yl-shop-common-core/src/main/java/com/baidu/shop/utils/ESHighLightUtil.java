package com.baidu.shop.utils;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ESHighLightUtil
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/16
 * @Version V1.0
 **/
public class ESHighLightUtil<T> {
    //构建高亮字段buiilder
    public static HighlightBuilder getHighlightBuilder(String ...highLightField){

        HighlightBuilder highlightBuilder = new HighlightBuilder();

        Arrays.asList(highLightField).forEach(hlf -> {
            HighlightBuilder.Field field = new HighlightBuilder.Field(hlf);

            field.preTags("<font style='color:red'>");
            field.postTags("</font>");

            highlightBuilder.field(field);
        });

        return highlightBuilder;
    }

    //将返回的内容替换成高亮
    public static <T> List<SearchHit<T>> getHighLightHit(List<SearchHit<T>> list){

        return list.stream().map(hit -> {
            //得到高亮字段
            Map<String, List<String>> highlightFields = hit.getHighlightFields();

            highlightFields.forEach((key,value) -> {
                try {
                    T content = hit.getContent();//当前文档 T为当前文档类型

                    //content.getClass()获取当前文档类型,并且得到排序字段的set方法
                    Method method = content.getClass().getMethod("set" + String.valueOf(key.charAt(0)).toUpperCase() + key.substring(1),String.class);

                    //执行set方法并赋值
                    method.invoke(content,value.get(0));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            });

            return hit;
        }).collect(Collectors.toList());

    }

}
