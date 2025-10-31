package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.*;
import com.mok.ddd.application.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<Page<RoleDTO>> findPage(@ParameterObject RoleQuery query, Pageable pageable) {
        Page<RoleDTO> page = roleService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<RoleDTO> getById(@PathVariable Long id) {
        RoleDTO roleDTO = roleService.getById(id);
        return RestResponse.success(roleDTO);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public RestResponse<RoleDTO> save(@RequestBody @Valid RoleSaveDTO roleSaveDTO) {
        RoleDTO savedRole = roleService.createRole(roleSaveDTO);
        return RestResponse.success(savedRole);
    }

    @Operation(summary = "修改角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public RestResponse<RoleDTO> update(@PathVariable Long id, @RequestBody @Valid RoleSaveDTO roleSaveDTO) {
        roleSaveDTO.setId(id);
        RoleDTO updatedRole = roleService.updateRole(roleSaveDTO);
        return RestResponse.success(updatedRole);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        roleService.deleteById(id);
        return RestResponse.success();
    }

    @Operation(summary = "获取角色的菜单")
    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<Set<MenuDTO>> getMenus(@PathVariable Long id) {
        Set<MenuDTO> menus = roleService.getMenusByRole(id);
        return RestResponse.success(menus);
    }

    @Operation(summary = "获取角色的权限")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<Set<PermissionDTO>> getPermissions(@PathVariable Long id) {
        Set<PermissionDTO> permissions = roleService.getPermissionsByRole(id);
        return RestResponse.success(permissions);
    }
}