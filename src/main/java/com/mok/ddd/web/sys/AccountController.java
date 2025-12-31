package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.sys.dto.auth.SelfPasswordUpdateDTO;
import com.mok.ddd.application.sys.dto.user.UserDTO;
import com.mok.ddd.application.sys.dto.user.UserPasswordDTO;
import com.mok.ddd.application.sys.service.UserService;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.web.common.RestResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @PutMapping("/password")
    public RestResponse<Boolean> changeMyPassword(@RequestBody @Valid SelfPasswordUpdateDTO dto) {
        UserDTO user = userService.findByUsername(TenantContextHolder.getUsername());

        UserPasswordDTO passwordDTO = new UserPasswordDTO();
        passwordDTO.setId(user.getId());
        passwordDTO.setPassword(dto.getNewPassword());
        userService.updatePassword(passwordDTO);
        return RestResponse.success(true);
    }

    @GetMapping("/info")
    public RestResponse<AccountInfoDTO> getMyInfo() {
        AccountInfoDTO user = userService.findAccountInfoByUsername(TenantContextHolder.getUsername());
        return RestResponse.success(user);
    }

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