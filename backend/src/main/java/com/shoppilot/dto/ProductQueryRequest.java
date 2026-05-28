package com.shoppilot.dto;

import lombok.Data;

@Data
public class ProductQueryRequest {

    private Long page = 1L;

    private Long size = 10L;

    private String keyword;

    private Long categoryId;

    private Integer status;
}
