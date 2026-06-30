package org.example.listener;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

// 这个注解告诉 MQ：我要监听 "order-success-topic" 这个主题的消息
@Component
@RocketMQMessageListener(topic = "order-success-topic", consumerGroup = "notify-consumer-group")
public class OrderNotificationListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        // 当 MQ 里有新消息时，会自动触发这个方法
        System.out.println("=========================================");
        System.out.println("【异步任务触发】短信中心收到 MQ 消息！");
        System.out.println("正在向用户发送通知短信，内容: " + message);
        System.out.println("=========================================");
    }
}