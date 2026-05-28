package com.shoppilot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class HealthResponse {

    private String status;
    private String message;
    private OffsetDateTime timestamp;
}
