package com.hmdp;

import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    //线程池
    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        // TODO 记得学juc
        //这个对象用于等待其他任务完成，当计数器为0时，表示所有任务都已完成。
        CountDownLatch latch = new CountDownLatch(100);

        Runnable task = () -> {
            //一百个任务
            for (int i = 0; i < 10; i++) {
                // 获取id
                long id = redisIdWorker.nextId("order");
                System.out.println("id = "+id);
            }
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        es.shutdown();
        System.out.println("耗时："+(end-begin));
        // es.shutdown();
        Assertions.assertEquals(300, 300);
    }


    @Test
    void testSaveShop() throws InterruptedException {
        shopService.saveShop2Redis(1L,10L);
        Assertions.assertEquals(300, 300);
    }
}
