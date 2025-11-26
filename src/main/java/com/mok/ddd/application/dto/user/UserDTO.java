package com.mok.ddd.application.dto.user;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private Integer state;
    private LocalDateTime createTime;
}