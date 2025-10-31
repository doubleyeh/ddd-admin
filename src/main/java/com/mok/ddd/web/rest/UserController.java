package com.mok.ddd.web.rest;

import com.mok.ddd.application.UserService;
import com.mok.ddd.application.dto.*;
import com.mok.ddd.common.Const;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户分页列表")
    @GetMapping
    public RestResponse<Page<UserDTO>> findPage(@ParameterObject UserQuery query, Pageable pageable) {
        Page<UserDTO> page = userService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public RestResponse<UserDTO> getById(@PathVariable Long id) {
        UserDTO userDTO = userService.getById(id);
        if(Objects.isNull(userDTO)){
            return RestResponse.failure(404, Const.NOT_FOUND_MESSAGE);
        }
        return RestResponse.success(userDTO);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public RestResponse<UserDTO> save(@RequestBody @Valid UserPostDTO userDTO) {
        UserDTO savedUser = userService.create(userDTO);
        return RestResponse.success(savedUser);
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    public RestResponse<UserDTO> update(@PathVariable Long id, @RequestBody @Valid UserPutDTO userDTO) {
        userDTO.setId(id);
        UserDTO updatedUser = userService.updateUser(userDTO);
        return RestResponse.success(updatedUser);
    }

    @Operation(summary = "修改用户密码")
    @PutMapping("/{id}/password")
    public RestResponse<Boolean> changePassword(@PathVariable Long id, @RequestBody @Valid UserPasswordDTO passwordDTO) {
        passwordDTO.setId(id);
        userService.updatePassword(passwordDTO);
        return RestResponse.success(true);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return RestResponse.success();
    }
}