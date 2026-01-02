package com.mok.ddd.web.sys;


import com.mok.ddd.application.sys.dto.tenant.*;
import com.mok.ddd.application.sys.service.TenantService;
import com.mok.ddd.infrastructure.log.annotation.OperLog;
import com.mok.ddd.infrastructure.log.enums.BusinessType;
import com.mok.ddd.web.common.RestResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasAuthority('tenant:list')")
    public RestResponse<Page<@NonNull TenantDTO>> findPage(TenantQuery query, Pageable pageable) {
        Page<@NonNull TenantDTO> page = tenantService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:list')")
    public RestResponse<TenantDTO> getById(@PathVariable Long id) {
        TenantDTO tenantDTO = tenantService.getById(id);
        return RestResponse.success(tenantDTO);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('tenant:create')")
    @OperLog(title = "租户管理", businessType = BusinessType.INSERT)
    public RestResponse<TenantCreateResultDTO> create(@RequestBody @Valid TenantSaveDTO tenantSaveDTO) {
        TenantCreateResultDTO result = tenantService.createTenant(tenantSaveDTO);
        return RestResponse.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:update')")
    @OperLog(title = "租户管理", businessType = BusinessType.UPDATE)
    public RestResponse<TenantDTO> update(@PathVariable Long id, @RequestBody @Valid TenantSaveDTO tenantSaveDTO) {
        tenantSaveDTO.setId(id);
        TenantDTO updatedTenant = tenantService.updateTenant(id, tenantSaveDTO);
        return RestResponse.success(updatedTenant);
    }

    @PutMapping("/{id}/state")
    @PreAuthorize("hasAuthority('tenant:update')")
    @OperLog(title = "租户管理", businessType = BusinessType.UPDATE)
    public RestResponse<TenantDTO> updateState(@PathVariable Long id,@Valid @NotNull @RequestParam Boolean state) {
        TenantDTO updatedTenant = tenantService.updateTenantState(id, state);
        return RestResponse.success(updatedTenant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:delete')")
    @OperLog(title = "租户管理", businessType = BusinessType.DELETE)
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        tenantService.deleteByVerify(id);
        return RestResponse.success();
    }

    @GetMapping("/options")
    public RestResponse<List<TenantOptionDTO>> getOptions(@RequestParam(required = false) String name) {
        return RestResponse.success(tenantService.findOptions(name));
    }
}
