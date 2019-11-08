package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class RedisPoolUtil {

    //设值
    public static String set(String key ,String value){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getRedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{}",key,value,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        return result;
    }

    //取值
    public static String get(String key){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getRedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key:{}",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        return result;
    }

    //设值key,value和过期时间
    public static String setex(String key,String value,int exTime){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getRedis();
            result = jedis.setex(key,exTime,value);
        } catch (Exception e) {
            log.error("setex key:{} value:{} exTime:{}",key ,value ,exTime ,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        return result;
    }

    /**
     * 设置key的过期时间
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key,int exTime){
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getRedis();
            result = jedis.expire(key,exTime);
        } catch (Exception e) {
            log.error("expire key:{} exTime:{}",key  ,exTime ,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        return result;
    }

    public static Long del(String key){
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getRedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{}",key ,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        return result;
    }

    /**
     * 测试API
     * @param args
     */
    public static void main(String[] args) {

        RedisPoolUtil.set("testKey","testValue");
        System.out.println(RedisPoolUtil.get("testKey"));
        RedisPoolUtil.setex("test1","value1",60*10);
        RedisPoolUtil.expire("testKey",60*5);
        //RedisPoolUtil.del("test1");
    }
}
