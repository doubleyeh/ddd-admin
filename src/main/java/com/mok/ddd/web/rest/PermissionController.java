package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.dto.permission.PermissionQuery;
import com.mok.ddd.application.service.PermissionService;
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
        return RestResponse.success(permissionService.save(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<Void> delete(@PathVariable Long id) {
        permissionService.deleteById(id);
        return RestResponse.success();
    }
}