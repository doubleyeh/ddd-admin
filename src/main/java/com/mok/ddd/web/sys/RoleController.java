package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.role.*;
import com.mok.ddd.application.sys.service.RoleService;
import com.mok.ddd.web.common.RestResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{id}/state")
    @PreAuthorize("hasAuthority('role:update')")
    public RestResponse<RoleDTO> updateState(@PathVariable Long id, @Valid @NotNull @RequestParam Boolean state) {
        RoleDTO dto = roleService.updateState(id, state);
        return RestResponse.success(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        roleService.deleteRoleBeforeValidation(id);
        return RestResponse.success();
    }

    @PostMapping("/{id}/grant")
    @PreAuthorize("hasAuthority('role:update')")
    public RestResponse<Void> grant(@PathVariable Long id, @RequestBody RoleGrantDTO grantDTO) {
        roleService.grant(id, grantDTO);
        return RestResponse.success();
    }

    @GetMapping("/options")
    public RestResponse<List<RoleOptionDTO>> getRoleOptions(RoleQuery query) {
        List<RoleOptionDTO> options = roleService.getRoleOptions(query);
        return RestResponse.success(options);
    }
}