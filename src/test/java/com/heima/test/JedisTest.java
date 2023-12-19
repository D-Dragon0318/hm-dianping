package com.heima.test;

import com.hmdp.utils.JedisConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @Author: Spridra
 * @CreateTime: 2023-12-18 15:54
 * @Describe:
 * @Version: 1.0
 */

public class JedisTest {
    private Jedis jedis;

    @BeforeEach
    void setUp() {
        //1.建立连接
//        jedis = new Jedis("127.0.0.1", 6379);
        jedis = JedisConnectionFactory.getJedisPool();
        //2.设置密码
        jedis.auth("123456");
        //3.选择库
        jedis.select(0);
    }

    @Test
    void testString() {
        String result = jedis.set("name", "spridra");
        System.out.println("result = " + result);
        //获取数据
        String name = jedis.get("name");
        System.out.println("name = " + name);
    }

    @Test
    void testHash() {
        jedis.hset("user:1", "name", "spridra");
        jedis.hset("user:1", "age", "20");

        Map<String,String> map = jedis.hgetAll("user:1");
        System.out.println(map);
    }

    @AfterEach
    void tearDown() {
        if (jedis != null){
            jedis.close();
        }
    }
}
