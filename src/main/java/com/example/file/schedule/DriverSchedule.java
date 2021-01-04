package com.example.file.schedule;

import com.example.file.constant.ScheduleLockKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lize
 * @Date: 2020/12/19 10:21
 * @Description:
 */
@Slf4j
@Component
@EnableScheduling
public class DriverSchedule extends ScheduleLock{

    public DriverSchedule(StringRedisTemplate stringRedisTemplate) {
        super(stringRedisTemplate, ScheduleLockKeyConstant.CAPITAL_LOCK_KEY);
    }

    /**
     * 5分钟执行一次
     */
    @Scheduled(cron = "${gov.schedule.cron}")
    public void schedule() {
//        govDriverManager.uploadDriverToGov(null, null, null);

        execute();
    }

    @Override
    public void handler() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("DriverSchedule2-" + Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND));
    }
}
