package com.mok.ddd.web.rest;

import java.io.Serializable;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mok.ddd.application.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.dto.auth.SelfPasswordUpdateDTO;
import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.dto.user.UserPasswordDTO;
import com.mok.ddd.application.service.UserService;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @Operation(summary = "用户更新密码")
    @PutMapping("/password")
    public RestResponse<Boolean> changeMyPassword(@RequestBody @Valid SelfPasswordUpdateDTO dto) {
        UserDTO user = userService.findByUsername(TenantContextHolder.getUsername());

        UserPasswordDTO passwordDTO = new UserPasswordDTO();
        passwordDTO.setId(user.getId());
        passwordDTO.setPassword(dto.getNewPassword());
        userService.updatePassword(passwordDTO);
        return RestResponse.success(true);
    }

    @Operation(summary = "用户信息")
    @GetMapping("/info")
    public RestResponse<AccountInfoDTO> getMyInfo() {
        AccountInfoDTO user = userService.findAccountInfoByUsername(TenantContextHolder.getUsername());
        return RestResponse.success(user);
    }

    @Operation(summary = "更新用户昵称")
    @PutMapping("/nickname")
    public RestResponse<Boolean> updateNickname(@RequestBody @Valid NicknameUpdateDTO dto) {
        String username = TenantContextHolder.getUsername();
        UserDTO user = userService.findByUsername(username);
        user.setNickname(dto.getNickname());
        userService.save(user);
        return RestResponse.success(true);
    }

    @Data
    public static class NicknameUpdateDTO implements Serializable {

        @NotBlank(message = "昵称不允许为空")
        @Size(max = 50, message = "昵称长度不能超过50个字符")
        private String nickname;
    }
}