package com.baidu.shop.redis.repository;

import com.baidu.shop.utils.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisRepository
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Component
public class RedisRepository {

    //--注意：此处不能使用Resource注解，因为在RedisConfig line:30行中使用@Bean注解，方法的返回值是RedisTemplate @Resource默认按名称自动注入，会与我们定义的redisTemplate冲突
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取序列化工具
     * @return
     */
    private RedisSerializer<String> getSerializer(){

        return redisTemplate.getStringSerializer();
    }

    /**
     * 放入string类型的值
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, final String value) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {

                return connection.set(getSerializer().serialize(key), getSerializer().serialize(value));
            }
        });
    }

    /**
     * 放入对象类型的值
     * @param key
     * @param obj
     * @return
     */
    public boolean setObj(final String key, final Object obj) {

        return redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {

                return connection.set(getSerializer().serialize(key), getSerializer().serialize(JSONUtil.toJsonString(obj)));
            }
        });
    }

    /**
     * 获取string类型的值
     * @param key
     * @return
     */
    public String get(final String key) {

        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {

                byte[] value = connection.get(getSerializer().serialize(key));

                return getSerializer().deserialize(value);
            }
        });
    }

    /**
     * 获取对象类型的值
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getObj(final String key,Class<T> clazz) {

        String result = redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {

                byte[] value = connection.get(getSerializer().serialize(key));

                return getSerializer().deserialize(value);
            }
        });

        Object o = JSONUtil.toBean(result, clazz);

        if(clazz.isInstance(o)){

            return clazz.cast(o);
        }

        return null;
    }

    /**
     * 给key值设置过期时间
     * @param key
     * @param expire
     * @return
     */
    public boolean expire(final String key, long expire) {
        return redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    /**
     * 放入list类型的值
     * @param key
     * @param list
     * @param <T>
     * @return
     */
    public <T> boolean setList(String key, List<T> list) {

        return set(key, JSONUtil.toJsonString(list));
    }

    /**
     * 获取list类型的值
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String key, Class<T> clz) {

        String json = get(key);

        if (json != null) {

            List<T> list = JSONUtil.toList(json, clz);
            return list;
        }
        return null;
    }

    /**
     * 操作队列
     * @param key
     * @param obj
     * @return
     */
    public long rpush(final String key, Object obj) {
        final String value = JSONUtil.toJsonString(obj);
        long result = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {

                long count = connection.rPush(getSerializer().serialize(key), getSerializer().serialize(value));
                return count;
            }
        });
        return result;
    }

    /**
     * 删除队列
     * @param key
     * @return
     */
    public String lpop(final String key) {
        String result = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {

                byte[] res = connection.lPop(getSerializer().serialize(key));
                return getSerializer().deserialize(res);
            }
        });
        return result;
    }

    /**
     * 通过key值删除缓存数据
     * @param key
     * @return
     */
    public boolean del(String key) {
        // TODO Auto-generated method stub
        return redisTemplate.delete(key);
    }

    /**
     * 存入hash类型的值
     * @param key
     * @param mapKey
     * @param value
     * @return
     */
    public boolean setHash(final String key,final String mapKey, final String value) {

        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                // TODO Auto-generated method stub

                return connection.hSet(getSerializer().serialize(key), getSerializer().serialize(mapKey), getSerializer().serialize(value));
            }
        });
    }

    /**
     * 通过redis的key和hashkey删除value
     * @param key
     * @param mapKey
     * @return
     */
    public boolean delHash(final String key,final String mapKey) {

        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                // TODO Auto-generated method stub
                Long aLong = connection.hDel(getSerializer().serialize(key), getSerializer().serialize(mapKey));
                return aLong != 0;
            }
        });
    }

    /**
     * 通过rediskey删除hash
     * @param key
     * @return
     */
    public boolean delHash(final String key) {

        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                // TODO Auto-generated method stub
                Long aLong = connection.hDel(getSerializer().serialize(key));
                return aLong != 0;
            }
        });
    }


    /**
     * 根据redis的key和hask的key获取hash对应的值
     * @param key
     * @param mapKey
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getHash(final String key,final String mapKey,Class<T> clazz) {

        String result = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {

                byte[] bytes = connection.hGet(getSerializer().serialize(key), getSerializer().serialize(mapKey));

                return getSerializer().deserialize(bytes);
            }
        });

        Object o = JSONUtil.toBean(result, clazz);
        if(clazz.isInstance(o)){
            return clazz.cast(o);
        }
        return null;
    }

    /**
     * 根据redis 的key值获取hash的entry
     * @param key
     * @return
     */
    public Map<String, String> getHash(final String key) {

        Map<byte[], byte[]> result = (Map<byte[], byte[]>) redisTemplate.execute(new RedisCallback<Map<byte[], byte[]>>() {
            @Override
            public Map<byte[], byte[]> doInRedis(RedisConnection connection) throws DataAccessException {

                Map<byte[], byte[]> map = connection.hGetAll(getSerializer().serialize(key));
                return map;
            }
        });

        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<byte[], byte[]> entry : result.entrySet()){

            map.put(getSerializer().deserialize(entry.getKey()),getSerializer().deserialize(entry.getValue()));
        }

        return map;
    }
}
