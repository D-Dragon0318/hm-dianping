package com.hmdp.utils;

import redis.clients.jedis.*;

/**
 * @Author: Spridra
 * @CreateTime: 2023-12-18 16:15
 * @Describe:
 * @Version: 1.0
 */

public class JedisConnectionFactory {
    private static JedisPool jedisPool;

    static {
        //配置连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 最大连接
        poolConfig.setMaxTotal(8);
        // 最大空闲连接
        poolConfig.setMaxIdle(8);
        // 最小空闲连接
        poolConfig.setMinIdle(0);
        // 设置最长等待时间， ms
        poolConfig.setMaxWaitMillis(200);
        // 创建连接池对象，参数：连接池配置、服务端ip、服务端端口、超时时间、密码
        jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379, 1000, "123456");
    }

    // 获取Jedis对象
    public static Jedis getJedisPool() {
        return jedisPool.getResource();
    }
}
