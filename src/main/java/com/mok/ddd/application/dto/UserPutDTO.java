package com.mok.ddd.application.dto;

import lombok.Data;

@Data
public class UserPutDTO {
    private Long id;
    private String username;
    private String nickname;
    private Integer state;
    private String password;
}