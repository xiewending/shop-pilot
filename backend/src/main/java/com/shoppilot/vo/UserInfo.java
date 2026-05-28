package com.shoppilot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {

    private Long id;
    private String username;
    private String nickname;
}
