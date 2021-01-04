package com.example.file.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import sun.rmi.runtime.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lize
 * @Date: 2020/12/29 16:36
 * @Description:
 */
@Slf4j
public abstract class ScheduleLock {

    StringRedisTemplate stringRedisTemplate;
    String key;

    public ScheduleLock(StringRedisTemplate stringRedisTemplate, String key) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.key = key;
    }

    public void execute() {
        Boolean ifAbsent = null;
        try {
            ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1");
            System.out.println("ifAbsent-" + ifAbsent + "-" + Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND));
            if (ifAbsent != null && ifAbsent) {
                stringRedisTemplate.opsForValue().getOperations().expire(key,3, TimeUnit.MINUTES);
                handler();
            }
        } catch (Exception e) {
            log.error("redis操作异常：" + e.getMessage());
        } finally {
            if (ifAbsent != null && ifAbsent) {
                stringRedisTemplate.opsForValue().getOperations().delete(key);
            }
        }
    }

    public abstract void handler();
}
