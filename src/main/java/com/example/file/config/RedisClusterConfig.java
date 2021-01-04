package com.example.file.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义redis connection factory 同时支持单机与集群配置 <br>
 * TODO 并支持redis密码的加密 <br>
 * 若要增加更多配置项 则在lettuceConnectionFactory()中进行对应设置即可
 *
 * @author .
 * @version 1.0.0
 * @date 2020-12-05 15:19
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisClusterConfig {

    private final RedisProperties redisProperties;

    @Autowired
    public RedisClusterConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * 重新定义 RedisTemplate
     *
     * @param redisConnectionFactory redis连接工厂
     * @return RedisTemplate<Object, Object>
     * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * 重新定义 StringRedisTemplate
     *
     * @param redisConnectionFactory redis连接工厂
     * @return StringRedisTemplate
     * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * Lettuce redis连接工厂
     *
     * @return LettuceConnectionFactory
     * //     * @see org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration
     */
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisClusterConfiguration clusterConfiguration = getClusterConfiguration();
        if (clusterConfiguration != null) {
            return new LettuceConnectionFactory(clusterConfiguration);
        } else {
            log.error("redis set cluster mode support!");
            throw new IllegalStateException("can't find redis cluster setting!");
        }
    }

    protected final RedisClusterConfiguration getClusterConfiguration() {
        RedisProperties.Cluster clusterProperties = this.redisProperties.getCluster();
        if (clusterProperties == null) {
            return null;
        }
        RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        //如果需要加密密码-配置使用com.alibaba.druid.filter.config.ConfigTools.decrypt解密
        if (this.redisProperties.getPassword() != null) {
            config.setPassword(RedisPassword.of(this.redisProperties.getPassword()));
        }
        return config;
    }

    /**
     * 分布式锁通用
     * //     * @see org.redisson.spring.starter.RedissonAutoConfiguration#redisson()
     *
     * @return RedissonClient
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {

        Duration timeout = redisProperties.getTimeout();
        if (timeout == null) {
            //定义3秒超时
            timeout = Duration.ofSeconds(3);
        }
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(convert(redisProperties.getCluster().getNodes()))
                .setConnectTimeout((int) timeout.toMillis())
                //如果需要加密密码-配置使用com.alibaba.druid.filter.config.ConfigTools.decrypt解密
                .setPassword(redisProperties.getPassword());

        return Redisson.create(config);
    }

    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<String>(nodesObject.size());
        boolean isSsl = redisProperties.isSsl();
        for (String node : nodesObject) {
            if (isSsl) {
                nodes.add("rediss://" + node);
            } else {
                nodes.add("redis://" + node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }
}
