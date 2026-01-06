package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import com.mok.ddd.application.sys.dto.permission.PermissionQuery;
import com.mok.ddd.application.sys.service.PermissionService;
import com.mok.ddd.web.common.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/menu/{menuId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<List<PermissionDTO>> findByMenuId(@PathVariable Long menuId) {
        PermissionQuery query = new PermissionQuery();
        query.setMenuId(menuId);
        return RestResponse.success(permissionService.findAll(query.toPredicate()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<PermissionDTO> save(@RequestBody PermissionDTO dto) {
        return RestResponse.success(permissionService.createPermission(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<PermissionDTO> update(@PathVariable Long id, @RequestBody PermissionDTO dto) {
        dto.setId(id);
        return RestResponse.success(permissionService.updatePermission(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<Void> delete(@PathVariable Long id) {
        permissionService.deleteById(id);
        return RestResponse.success();
    }
}