package com.example.file.constant;

/**
 * @Author: lize
 * @Date: 2020/12/18 10:41
 * @Description:
 */
public class MQRoutingKeyConstant {


    /**
     * logging模块主题交换机
     */
    public static final String SWLHY_LOGGING_EXCHANGE = "swlhy-logging.exchange";
    /**
     * 接收企业数据保存到日志表后发送mq消息key
     */
    public static final String SWLHY_LOGGING_RECEIVE_ROUTINGKEY = "swlhy-logging.receive.routingKey";
}
