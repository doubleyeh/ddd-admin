package com.mok.ddd.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPutDTO {
    private Long id;

    @Size(min = 4, max = 50, message = "用户名长度需在4到50个字符之间")
    private String username;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @NotNull(message = "用户状态不能为空")
    private Integer state;

    private String password;
}