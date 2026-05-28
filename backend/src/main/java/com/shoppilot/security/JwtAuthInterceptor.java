package com.shoppilot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppilot.common.ApiResponse;
import com.shoppilot.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public JwtAuthInterceptor(JwtTokenProvider jwtTokenProvider, TokenService tokenService, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }

        String token = authorization.substring(7);
        if (!tokenService.isTokenValid(token)) {
            writeUnauthorized(response);
            return false;
        }

        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            LoginUserContext.set(new LoginUserContext.LoginUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class),
                    claims.get("nickname", String.class)
            ));
            return true;
        } catch (Exception exception) {
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(401, "未登录或登录已过期")));
    }
}
