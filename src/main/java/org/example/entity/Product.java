package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_product")
public class Product {
    @TableId(type = IdType.AUTO)
    public Long id;
    public String productName;
    public Integer stock;
}