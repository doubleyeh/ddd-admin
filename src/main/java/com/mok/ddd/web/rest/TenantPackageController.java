package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.dto.tenantPackage.*;
import com.mok.ddd.application.service.TenantPackageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tenant-packages")
@RequiredArgsConstructor
public class TenantPackageController {

    private final TenantPackageService packageService;

    @GetMapping
    @PreAuthorize("hasAuthority('tenantPackage:list')")
    public RestResponse<Page<@NonNull TenantPackageDTO>> findPage(TenantPackageQuery query, Pageable pageable) {
        Page<@NonNull TenantPackageDTO> page = packageService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tenantPackage:list')")
    public RestResponse<TenantPackageDTO> get(@PathVariable Long id) {
        return RestResponse.success(packageService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('tenantPackage:create')")
    public RestResponse<Void> create(@RequestBody TenantPackageSaveDTO dto) {
        packageService.createPackage(dto);
        return RestResponse.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tenantPackage:update')")
    public RestResponse<Void> update(@PathVariable Long id, @RequestBody TenantPackageSaveDTO dto) {
        packageService.updatePackage(id, dto);
        return RestResponse.success();
    }

    @PutMapping("/{id}/grant")
    @PreAuthorize("hasAuthority('tenantPackage:update')")
    public RestResponse<Void> grant(@PathVariable Long id, @RequestBody TenantPackageGrantDTO dto) {
        packageService.grant(id, dto);
        return RestResponse.success();
    }

    @PutMapping("/{id}/state")
    @PreAuthorize("hasAuthority('tenantPackage:update')")
    public RestResponse<TenantPackageDTO> updateState(@PathVariable Long id, @Valid @NotNull @RequestParam Boolean state) {
        TenantPackageDTO updatedTenant = packageService.updateTenantState(id, state);
        return RestResponse.success(updatedTenant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tenantPackage:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        packageService.deleteByVerify(id);
        return RestResponse.success();
    }

    @GetMapping("/options")
    public RestResponse<List<TenantPackageOptionDTO>> getOptions(@RequestParam(required = false) String name) {
        return RestResponse.success(packageService.findOptions(name));
    }

}
