package com.shoppilot.dto;

import lombok.Data;

@Data
public class OrderQueryRequest {

    private Long page = 1L;

    private Long size = 10L;

    private String keyword;

    private Integer status;
}
