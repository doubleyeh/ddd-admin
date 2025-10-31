package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.SelfPasswordUpdateDTO;
import com.mok.ddd.application.dto.UserDTO;
import com.mok.ddd.application.dto.UserPasswordDTO;
import com.mok.ddd.application.service.UserService;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}