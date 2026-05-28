package com.shoppilot.security;

import lombok.AllArgsConstructor;
import lombok.Data;

public class LoginUserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    @AllArgsConstructor
    public static class LoginUser {

        private Long id;
        private String username;
        private String nickname;
    }
}
