package com.shoppilot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusRequest {

    @NotNull
    private Integer status;
}
