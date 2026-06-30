package org.example.service.impl;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.example.entity.Order;
import org.example.entity.Product;
import org.example.mapper.OrderMapper;
import org.example.mapper.ProductMapper;
import org.example.service.OrderService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedissonClient redissonClient;

    // 注入 RocketMQ 模板
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String makeOrder(String userId, String productId) {
        Long pid = Long.parseLong(productId);
        String lockKey = "lock:product:" + pid;

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!isLocked) {
                return "当前排队人数过多，请稍后再试！";
            }

            Product product = productMapper.selectById(pid);
            if (product == null || product.stock <= 0) {
                return "库存不足，下单失败！";
            }

            product.stock = product.stock - 1;
            productMapper.updateById(product);

            Order order = new Order();
            order.userId = userId;
            order.productId = pid;
            orderMapper.insert(order);

            // ================= 核心新增：发送 MQ 消息 =================
            // 主题 (Topic) 为 "order-success-topic"
            String message = "用户 [" + userId + "] 成功购买了商品 [" + productId + "]";
            rocketMQTemplate.convertAndSend("order-success-topic", message);
            // ==========================================================

            return "用户 " + userId + " 成功下单商品 " + productId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "系统繁忙，请重试";
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}