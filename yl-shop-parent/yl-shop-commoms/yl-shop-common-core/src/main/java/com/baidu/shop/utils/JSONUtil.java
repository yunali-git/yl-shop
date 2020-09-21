package com.baidu.shop.utils;

import com.alibaba.fastjson.JSONObject;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName JSONUtil
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/8/27
 * @Version V1.0
 **/
public class JSONUtil {
    private static Gson gson = null;

    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();// todo yyyy-MM-dd HH:mm:ss
    }

    public static Map<String, String> toMapValueString(String json) {

        Map<String, String> map = gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
        return map;
    }

    public static Map<String, List<String>> toMapValueStrList(String json) {

        Map<String, List<String>> map = gson.fromJson(json, new TypeToken<Map<String, List<String>>>() {}.getType());

        return map;
    }

    public static synchronized Gson newInstance() {
        if (gson == null) {
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        }
        return gson;
    }

    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T toBean(String json, Class<T> clz) {

        return gson.fromJson(json, clz);
    }

    public static <T> Map<String, T> toMap(String json, Class<T> clz) {
        Map<String, JsonObject> map = gson.fromJson(json, new TypeToken<Map<String, JsonObject>>() {
        }.getType());
        Map<String, T> result = new HashMap<String, T>();
        for (String key : map.keySet()) {
            result.put(key, gson.fromJson(map.get(key), clz));
        }
        return result;
    }

    public static Map<String, Object> toMap(String json) {
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        return map;
    }

    public static <T> List<T> toList(String json, Class<T> clz) {
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        List<T> list = new ArrayList<T>();
        for (final JsonElement elem : array) {
            list.add(gson.fromJson(elem, clz));
        }
        return list;
    }

    /**
     * 从json字符串中获取需要的值
     *
     * @param json
     * @param clazz 要转换的类型
     * @return
     */
    public static <T> Object getObjectByKey(String json, Class<T> clazz) {
        if (json != null && !"".equals(json)) {
            return JSONObject.parseObject(json, clazz);
        }
        return null;
    }

    /**
     * 从json字符串中获取需要的值
     *
     * @param json
     * @param clazz 要转换的类型
     * @return
     */
    public static <T> List<T> getListByKey(String json, Class<T> clazz) {
        if (json != null && !"".equals(json)) {
            return JSONObject.parseArray(json, clazz);
        }
        return null;
    }

    /**
     * 从json字符串中获取需要的值
     *
     * @param json
     * @param key
     *            键
     * @return
     */
    public static String getStrByKey(String json, String key) {
        String str = "";
        if (json != null && !"".equals(json)) {
            JSONObject j = JSONObject.parseObject(json);
            if (j.get(key) != null) {
                str = j.get(key).toString();
            }
        }
        return str;
    }

    /**
     * 向文件中写数据
     *
     * @param _sDestFile
     * @param _sContent
     * @throws IOException
     */
    public static void writeByFileOutputStream(String _sDestFile, String _sContent) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(_sDestFile);
            fos.write(_sContent.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
                fos = null;
            }

        }

    }

    /**
     * 非空
     *
     * @param str
     * @return true:不为空 false：空
     */
    public static boolean noEmpty(String str) {
        boolean flag = true;
        if ("".equals(str)) {
            flag = false;
        }
        return flag;
    }

    /**
     * 将"%"去掉
     *
     * @param str
     * @return
     */
    public static double getDecimalByPercentage(String str) {
        double fuse = 0.0;
        if (!"".equals(str) && str != null) {
            if (str.split("%").length > 0) {
                fuse = Double.parseDouble(str.split("%")[0]);
                return fuse;
            }
        }
        return 0.0;
    }

    /**
     * 保留2位小数
     *
     * @param number
     * @return
     */
    public static double ConversionFraction(double number) {
        return Math.floor(number * 100 + 0.5) / 100;
    }

    public static float ConversionM(double number) {
        return (float) JSONUtil.ConversionFraction(number / 1024 / 1024);
    }

    public static String getErrorText(String s) {
        JSONObject j = JSONObject.parseObject(s);
        return j.getJSONObject(j.keySet().iterator().next()).get("errortext").toString();
    }

    public static String getSingleJobId(String s) throws Exception {
        JSONObject j = JSONObject.parseObject(s);
        try {
            return j.getJSONObject(j.keySet().iterator().next()).get("jobid").toString();
        } catch (Exception e) {
            try {
                return j.getJSONObject(j.keySet().iterator().next()).get("errortext").toString();
            } catch (Exception e1) {
                throw new Exception(e1.getMessage());

            }

        }
    }

    public static <T> T readValue(String jsonStr, TypeReference type)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, type);
    }

    public static JSON_TYPE getJSONType(String str) {
        if (null == str || "".equals(str)) {
            return JSON_TYPE.JSON_TYPE_ERROR;
        }

        final char[] strChar = str.substring(0, 1).toCharArray();
        final char firstChar = strChar[0];

        if (firstChar == '{') {
            return JSON_TYPE.JSON_TYPE_OBJECT;
        } else if (firstChar == '[') {
            return JSON_TYPE.JSON_TYPE_ARRAY;
        } else {
            return JSON_TYPE.JSON_TYPE_ERROR;
        }
    }

    public enum JSON_TYPE {
        /** JSONObject */
        JSON_TYPE_OBJECT,
        /** JSONArray */
        JSON_TYPE_ARRAY,
        /** 不是JSON格式的字符串 */
        JSON_TYPE_ERROR
    }
}
