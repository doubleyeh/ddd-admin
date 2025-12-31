package com.mok.ddd.application.sys.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPasswordDTO {
    private Long id;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;
}
