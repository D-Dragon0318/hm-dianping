package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Spridra
 * @CreateTime: 2023-12-19 14:59
 * @Describe:
 * @Version: 1.0
 */

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.判断释放需要拦截（threadLocal是否有用户）
        if (UserHolder.getUser()==null){
            //没有，需要拦截
            response.setStatus(401);
            return false;
        }
        return true;
    }


}
