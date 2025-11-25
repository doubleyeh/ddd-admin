package com.mok.ddd.web.rest;

import java.util.Objects;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.dto.user.UserPasswordDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.dto.user.UserPutDTO;
import com.mok.ddd.application.dto.user.UserQuery;
import com.mok.ddd.application.service.UserService;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public RestResponse<Page<UserDTO>> findPage(@ParameterObject UserQuery query, Pageable pageable) {
        Page<UserDTO> page = userService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:list')")
    public RestResponse<UserDTO> getById(@PathVariable Long id) {
        UserDTO userDTO = userService.getById(id);
        if (Objects.isNull(userDTO)) {
            return RestResponse.failure(404, Const.NOT_FOUND_MESSAGE);
        }
        return RestResponse.success(userDTO);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public RestResponse<UserDTO> save(@RequestBody @Valid UserPostDTO userDTO) {
        UserDTO savedUser = userService.create(userDTO);
        return RestResponse.success(savedUser);
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public RestResponse<UserDTO> update(@PathVariable Long id, @RequestBody @Valid UserPutDTO userDTO) {
        userDTO.setId(id);
        UserDTO updatedUser = userService.updateUser(userDTO);
        return RestResponse.success(updatedUser);
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('user:update')")
    public RestResponse<String> resetPassword(@PathVariable Long id) {
        String newPassword = PasswordGenerator.generateRandomPassword();

        UserPasswordDTO passwordDTO = new UserPasswordDTO();
        passwordDTO.setId(id);
        passwordDTO.setPassword(newPassword);

        userService.updatePassword(passwordDTO);
        return RestResponse.success(newPassword);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return RestResponse.success();
    }
}