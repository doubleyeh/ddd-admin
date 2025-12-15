package com.mok.ddd.web.rest;

import java.util.Objects;


import com.mok.ddd.application.dto.tenant.TenantDTO;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.dto.user.UserPasswordDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.dto.user.UserPutDTO;
import com.mok.ddd.application.dto.user.UserQuery;
import com.mok.ddd.application.service.UserService;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public RestResponse<Page<@NonNull UserDTO>> findPage(UserQuery query, Pageable pageable) {
        Page<@NonNull UserDTO> page = userService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:list')")
    public RestResponse<UserDTO> getById(@PathVariable Long id) {
        UserDTO userDTO = userService.getById(id);
        if (Objects.isNull(userDTO)) {
            return RestResponse.failure(404, Const.NOT_FOUND_MESSAGE);
        }
        return RestResponse.success(userDTO);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public RestResponse<UserDTO> save(@RequestBody @Valid UserPostDTO userDTO) {
        UserDTO savedUser = userService.create(userDTO);
        return RestResponse.success(savedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public RestResponse<UserDTO> update(@PathVariable Long id, @RequestBody @Valid UserPutDTO userDTO) {
        userDTO.setId(id);
        UserDTO updatedUser = userService.updateUser(userDTO);
        return RestResponse.success(updatedUser);
    }

    @PutMapping("/{id}/state")
    @PreAuthorize("hasAuthority('user:update')")
    public RestResponse<UserDTO> updateState(@PathVariable Long id, @Valid @NotNull @RequestParam Integer state) {
        UserDTO dto = userService.updateUserState(id, state);
        return RestResponse.success(dto);
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return RestResponse.success();
    }
}