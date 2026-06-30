package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    public Long id;
    public String userId;
    public Long productId;
}