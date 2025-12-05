package com.mok.ddd.web.rest;

import java.util.Set;


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

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.dto.role.RoleDTO;
import com.mok.ddd.application.dto.role.RoleQuery;
import com.mok.ddd.application.dto.role.RoleSaveDTO;
import com.mok.ddd.application.service.RoleService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<Page<RoleDTO>> findPage(RoleQuery query, Pageable pageable) {
        Page<RoleDTO> page = roleService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<RoleDTO> getById(@PathVariable Long id) {
        RoleDTO roleDTO = roleService.getById(id);
        return RestResponse.success(roleDTO);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public RestResponse<RoleDTO> save(@RequestBody @Valid RoleSaveDTO roleSaveDTO) {
        RoleDTO savedRole = roleService.createRole(roleSaveDTO);
        return RestResponse.success(savedRole);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public RestResponse<RoleDTO> update(@PathVariable Long id, @RequestBody @Valid RoleSaveDTO roleSaveDTO) {
        roleSaveDTO.setId(id);
        RoleDTO updatedRole = roleService.updateRole(roleSaveDTO);
        return RestResponse.success(updatedRole);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        roleService.deleteById(id);
        return RestResponse.success();
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<Set<MenuDTO>> getMenus(@PathVariable Long id) {
        Set<MenuDTO> menus = roleService.getMenusByRole(id);
        return RestResponse.success(menus);
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:list')")
    public RestResponse<Set<PermissionDTO>> getPermissions(@PathVariable Long id) {
        Set<PermissionDTO> permissions = roleService.getPermissionsByRole(id);
        return RestResponse.success(permissions);
    }
}