package com.shoppilot.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenService {

    private static final String TOKEN_KEY_PREFIX = "login:token:";

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String token, Long userId, long expirationSeconds) {
        redisTemplate.opsForValue().set(buildKey(token), userId, Duration.ofSeconds(expirationSeconds));
    }

    public boolean isTokenValid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(token)));
    }

    public void deleteToken(String token) {
        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
