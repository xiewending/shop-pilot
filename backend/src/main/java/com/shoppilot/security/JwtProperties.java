package com.shoppilot.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shop-pilot.jwt")
public class JwtProperties {

    private String secret;

    private Long expirationSeconds = 7200L;
}
