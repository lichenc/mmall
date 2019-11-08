package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    //JedisPool 连接池类
    private static JedisPool pool = null;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.tatol","20"));//最大连接数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));//最大空闲数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));//最小空闲数
    private static boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.onTestBorrow ","true"));;//在borrow一个实例的时候，是否需要需要测试，如果测试结果为true，说明实例可用
    private static boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.onTestReturn","true"));;//在return一个实例的时候，是否需要需要测试，如果测试结果为true，说明实例可用

    private static  String redisIp = PropertiesUtil.getProperty("redis.ip");//
    private static  Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));//

    //声明一个方法，用于初始化redis参数
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //该属性很重要，连接耗尽时，是否阻塞。如果设置为true，则超过最大
        //连接数的的连接需要等待，直到超时
        //如果设置为false，会抛出异常
        config.setBlockWhenExhausted(true);

        pool = new JedisPool(config ,redisIp ,redisPort ,1000*2);
    }
    //编写一个代码块，用于加载初始化redis参数
    static{
        initPool();
    }
    //获取一个jedis实例
    public static Jedis getRedis(){
        return pool.getResource();
    }
    //return一个broken的jedis实例
    public static void returnBrokenResource(Jedis jedis){
         pool.returnBrokenResource(jedis);
    }
    //return一个jedis实例
    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("lichenkey","lichenValue");
        returnResource(jedis);

        pool.destroy();

    }
}
