package com.shoppilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppilot.common.BusinessException;
import com.shoppilot.dto.LoginRequest;
import com.shoppilot.entity.SysUser;
import com.shoppilot.mapper.SysUserMapper;
import com.shoppilot.security.JwtTokenProvider;
import com.shoppilot.security.PasswordHasher;
import com.shoppilot.vo.LoginResponse;
import com.shoppilot.vo.UserInfo;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(SysUserMapper sysUserMapper, PasswordHasher passwordHasher, JwtTokenProvider jwtTokenProvider) {
        this.sysUserMapper = sysUserMapper;
        this.passwordHasher = passwordHasher;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));

        if (user == null || !passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用");
        }

        return new LoginResponse(
                jwtTokenProvider.createToken(user),
                "Bearer",
                jwtTokenProvider.getExpirationSeconds(),
                new UserInfo(user.getId(), user.getUsername(), user.getNickname())
        );
    }
}
