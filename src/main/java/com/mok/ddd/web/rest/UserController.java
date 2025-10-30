package com.mok.ddd.web.rest;

import com.mok.ddd.application.UserService;
import com.mok.ddd.application.dto.UserDTO;
import com.mok.ddd.application.dto.UserPostDTO;
import com.mok.ddd.application.dto.UserPutDTO;
import com.mok.ddd.application.dto.UserQuery;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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
        return RestResponse.success(userDTO);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public RestResponse<UserDTO> save(@RequestBody UserPostDTO userDTO) {
        UserDTO savedUser = userService.create(userDTO);
        return RestResponse.success(savedUser);
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    public RestResponse<UserDTO> update(@PathVariable Long id, @RequestBody UserPutDTO userDTO) {
        userDTO.setId(id);
        UserDTO updatedUser = userService.updateUser(userDTO);
        return RestResponse.success(updatedUser);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return RestResponse.success();
    }
}