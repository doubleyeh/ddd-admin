package com.mok.ddd.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO{
    private Long id;
    private String username;
    private String nickname;
    private Integer state;
    private LocalDateTime createTime;
}