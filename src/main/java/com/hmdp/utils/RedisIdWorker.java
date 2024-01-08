package com.hmdp.utils;

import cn.hutool.log.Log;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * @Author: Spridra
 * @CreateTime: 2023-12-21 16:47
 * @Describe: 订单ID唯一标识
 * @Version: 1.0
 */

@Component
public class RedisIdWorker {
    /**
     * 开始时间戳
     */
    //2022-01-01 00:00:00
   // private static final long BEGIN_TIMESTAMP = 1640995200L;
    //2023-01-01 00:00:00
    private static final long BEGIN_TIMESTAMP = 1672531200L;
    /**
     * 序列号的位数
     */
    private static final int COUNT_BITS = 32;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) throws NullPointerException {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 2.1.获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2.自增长
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }
    public static void main(String[] args) {
        /* String dateTimeStr = "2023-01-01 00:00:00";

        // 使用 Hutool 的 DateUtil 类将日期时间字符串转换为 Date 对象
        Date date = DateUtil.parse(dateTimeStr);

        // 获取时间戳
        long timestamp = date.getTime(); */

        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);


        System.out.println("时间戳: " + second);

    }
}
