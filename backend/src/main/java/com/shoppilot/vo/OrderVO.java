package com.shoppilot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private String shippingAddress;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
