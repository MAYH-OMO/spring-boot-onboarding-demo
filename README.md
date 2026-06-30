Spring Boot Onboarding Demo 
# 📖 项目简介
本项目是一个基于 Spring Boot 3 构建的高并发商品下单系统原型，专为涵盖后端开发核心技术栈而设计。项目模拟了真实电商场景中的“秒杀下单”全链路流程，重点解决了高并发下的超卖问题、数据库事务一致性问题，并通过消息队列实现了业务逻辑的异步解耦。



# 🛠 技术栈概览
核心框架： Java 21 + Spring Boot 3.2.4

持久层框架： MyBatis-Plus 3.5.5

数据库： MySQL 8.x

分布式缓存与锁： Redis + Redisson 3.27.0

消息队列： Apache RocketMQ 2.3.0 (适配 Spring Boot 3)

# 🚀 核心功能模块与技术亮点
## 1. MVC 架构与全局拦截器 (Interceptor)
功能说明： 实现了标准的三层架构（Controller-Service-Mapper）。

技术细节： 注册了全局 LoginInterceptor，对进入系统的请求进行统一的 Token 身份校验，保障接口安全性，未授权请求将直接被拦截并返回 401 状态码。

## 2. AOP 面向切面编程 (非侵入式监控)
功能说明： 实现接口级别的性能监控日志。

技术细节： 定义 @Around 环绕通知，拦截 controller 包下的所有请求，自动记录并打印每个接口的执行耗时，全程对业务代码零侵入。

## 3. MySQL 事务管理与索引优化
功能说明： 保障扣减库存与创建订单的数据一致性，优化查询性能。

技术细节： * 利用 @Transactional 注解实现强一致性事务，遇到异常自动回滚，杜绝“吞库存”现象。

为订单表的 user_id 字段建立了 B+ 树索引，将用户订单查询的时间复杂度从 O(N) 优化至 O(log N)。

## 4. Redis 分布式锁 (防超卖机制)
功能说明： 解决多线程/多节点并发下单时的库存超卖问题。

技术细节： 引入 Redisson 客户端，基于 Lua 脚本实现原子操作。采用细粒度锁（以 productId 为 Key），并设置了 3 秒排队等待与 10 秒看门狗租期，兼顾了高吞吐量与死锁防护。

## 5. RocketMQ 异步解耦 (削峰填谷)
功能说明： 将耗时的边缘业务（如发送短信通知）从主交易链路中剥离。

技术细节： 订单入库成功后，Producer 立刻投递消息至 order-success-topic 并返回给用户侧。Consumer 异步监听并消费消息，大幅降低接口响应延迟。

# 📁 核心代码结构
Plaintext

onboarding-demo

├── src/main/java/org/example

│   ├── aspect/            # AOP 切面类 (日志监控)

│   ├── config/            # Spring 全局配置 (拦截器注册)

│   ├── controller/        # REST 接口层

│   ├── entity/            # 数据库实体类 (MyBatis-Plus 映射)

│   ├── interceptor/       # 拦截器具体实现 (登录校验)

│   ├── listener/          # MQ 消费者监听器

│   ├── mapper/            # 数据访问层接口

│   ├── service/           # 业务逻辑层接口及实现类

│   └── Main.java          # Spring Boot 启动类

└── src/main/resources

    └── application.yml    # 核心配置文件 (数据库、Redis、MQ 均在此配置)
    
# ⚙️ 快速开始
## 1. 环境准备
请确保本地或 Docker 环境中已成功运行以下中间件：

MySQL (默认端口: 3306)

Redis (默认端口: 6379)

RocketMQ (NameServer 端口: 9876, Broker 端口: 10911)

## 2. 初始化数据库
执行以下 SQL 脚本建立测试数据：

SQL

CREATE DATABASE IF NOT EXISTS onboarding_demo DEFAULT CHARACTER SET utf8mb4;

USE onboarding_demo;


CREATE TABLE `t_product` (

  `id` BIGINT NOT NULL AUTO_INCREMENT,
  
  `product_name` VARCHAR(50) NOT NULL,
  
  `stock` INT NOT NULL,
  
  PRIMARY KEY (`id`)
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


INSERT INTO `t_product` (`id`, `product_name`, `stock`) VALUES (1, '掌上先机限量马克杯', 100);


CREATE TABLE `t_order` (

  `id` BIGINT NOT NULL AUTO_INCREMENT,
  
  `user_id` VARCHAR(50) NOT NULL,
  
  `product_id` BIGINT NOT NULL,
  
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  
  KEY `idx_user_id` (`user_id`) 
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

## 3. 修改配置
打开 src/main/resources/application.yml，修改以下信息为你本地的真实配置：

MySQL 的 username 和 password

Redis 的密码（如有设置）

## 4. 启动与测试
运行 Main.java 启动项目。

正常下单测试： 打开浏览器或 Postman 访问以下链接：
http://localhost:8080/order/create?userId=101&productId=1&token=admin

拦截器测试： 移除 URL 中的 token 参数，请求将被拒绝。

异步消费观察： 下单成功后，观察 IDEA 控制台，将输出 【异步任务触发】短信中心收到 MQ 消息！。
