package com.mok.ddd.application.dto;

import lombok.Data;

@Data
public class UserPostDTO {
    private String username;
    private String nickname;
    private Integer state;
    private String password;
}