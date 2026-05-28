package com.shoppilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class OrderRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String customerName;

    private String customerPhone;

    private BigDecimal totalAmount;

    private Integer status;

    private String shippingAddress;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
