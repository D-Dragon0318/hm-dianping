package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryOrderByAsc() {
        //1.从redis查询商铺缓存
        String shopTypeJson = stringRedisTemplate.opsForValue().get(CACHE_SHOPTYPE_KEY);
        //2.判断是否存在
        if (StrUtil.isNotBlank(shopTypeJson)) {
            //3.存在，直接返回
            List<ShopType> shopType = JSONUtil.toList(shopTypeJson, ShopType.class);
            return Result.ok(shopType);
        } else {
            //4.不存在，从数据库查询
            List<ShopType> typeList = query().orderByAsc("sort").list();
            //5.不存在，返回错误
            if (typeList == null) {
                //不存在则存空
//                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "");
                return Result.fail("请刷新");
            }
            //6.将查询结果存入redis
            stringRedisTemplate.opsForValue().set(CACHE_SHOPTYPE_KEY, JSONUtil.toJsonStr(typeList));
            return Result.ok(typeList);
        }
    }
}
