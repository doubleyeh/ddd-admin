package com.mok.ddd.web.rest;

import org.springdoc.core.annotations.ParameterObject;
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

import com.mok.ddd.application.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantQuery;
import com.mok.ddd.application.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.service.TenantService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "获取租户分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('tenant:list')")
    public RestResponse<Page<TenantDTO>> findPage(@ParameterObject TenantQuery query, Pageable pageable) {
        Page<TenantDTO> page = tenantService.findPage(query.toPredicate(), pageable);
        return RestResponse.success(page);
    }

    @Operation(summary = "获取租户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:list')")
    public RestResponse<TenantDTO> getById(@PathVariable Long id) {
        TenantDTO tenantDTO = tenantService.getById(id);
        return RestResponse.success(tenantDTO);
    }

    @Operation(summary = "新增租户")
    @PostMapping
    @PreAuthorize("hasAuthority('tenant:create')")
    public RestResponse<TenantCreateResultDTO> create(@RequestBody @Valid TenantSaveDTO tenantSaveDTO) {
        TenantCreateResultDTO result = tenantService.createTenant(tenantSaveDTO);
        return RestResponse.success(result);
    }

    @Operation(summary = "修改租户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:update')")
    public RestResponse<TenantDTO> update(@PathVariable Long id, @RequestBody @Valid TenantSaveDTO tenantSaveDTO) {
        tenantSaveDTO.setId(id);
        TenantDTO updatedTenant = tenantService.updateTenant(id, tenantSaveDTO);
        return RestResponse.success(updatedTenant);
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        tenantService.deleteByVerify(id);
        return RestResponse.success();
    }
}