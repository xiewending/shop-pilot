package com.shoppilot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shop-pilot.upload")
public class UploadProperties {

    private String baseDir = "../uploads";

    private String publicPath = "/uploads/";

    private long maxSize = 2 * 1024 * 1024;
}
