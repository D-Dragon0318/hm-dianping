package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 缓存穿透
//        Shop shop = queryWithPassThrough(id);

        //互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);
        if (shop == null) {
            return Result.fail("查询失败");
        }
        return Result.ok(shop);
    }

    public Shop queryWithMutex(Long id){
        //1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //不为 null，不为空字符串：""，不为空格、全角空格、制表符、换行符，等不可见字符
            //null代表缓存没有这个key-value，
            //空字符串代表这个key-value存在，但是值为空也就是后面逻辑置空
            //3.存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //这里往下要么是null要么是空字符串
        //判断缓存中命中的是否是空值
        if (shopJson != null){
            // 返回一个错误信息
            return null;
        }

        //4.实现缓存重建
        //4.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean idLock = tryLocck(lockKey);
            //4.2 判断是否获取成功
            if (!idLock) {
                //4.3 失败，则休眠重试
                Thread.sleep(50);
                return queryWithMutex(id);
            }

            //4.4 成功，从数据库查询
            shop = getById(id);
            //模拟重建延时
            Thread.sleep(200);
            //5.不存在，返回错误
            if (shop == null) {
                //不存在则存空
                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "",CACHE_NULL_TTL,TimeUnit.MINUTES);
                return null;
            }
            //6.将查询结果存入redis
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //7. 释放互斥锁
            unlock(lockKey);
        }

        //8.返回
        return shop;

    }
    public Shop queryWithPassThrough(Long id){
        //1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //不为 null，不为空字符串：""，不为空格、全角空格、制表符、换行符，等不可见字符
            //null代表缓存没有这个key-value，
            //空字符串代表这个key-value存在，但是值为空也就是后面逻辑置空
            //3.存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //这里往下要么是null要么是空字符串
        //判断缓存中命中的是否是空值
        if (shopJson != null){
            // 返回一个错误信息
            return null;
        }
        //4.不存在，从数据库查询
        Shop shop = getById(id);
        //5.不存在，返回错误
        if (shop == null) {
            //不存在则存空
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return null;
        }
        //6.将查询结果存入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;

    }

    /**
     * @description: 加锁
     * @methodname:  tryLocck
     * @return:      boolean
     */
    private boolean tryLocck(String key){
        //if absent 如果缺席
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    /**
     * @description: 去锁
     * @methodname:  unlock
     * @return:      void
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        //1.更新数据库
        updateById(shop);
        //2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }
}
