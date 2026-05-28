package com.shoppilot.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotProductVO {

    private Long id;

    private String name;

    private String categoryName;

    private BigDecimal price;

    private Integer stock;

    private Integer status;

    private Double score;
}
