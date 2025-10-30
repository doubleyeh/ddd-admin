package com.mok.ddd.application.dto;

import lombok.Data;

@Data
public class UserQuery {
    private String username;
    private String nickname;
    private Integer state;
}