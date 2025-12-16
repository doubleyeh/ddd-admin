package com.mok.ddd.web.rest;


import com.mok.ddd.application.dto.tenant.*;
import com.mok.ddd.application.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
    public RestResponse<Page<TenantDTO>> findPage(TenantQuery query, Pageable pageable) {
        Page<TenantDTO> page = tenantService.findPage(query.toPredicate(), pageable);
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
    public RestResponse<TenantCreateResultDTO> create(@RequestBody @Valid TenantSaveDTO tenantSaveDTO) {
        TenantCreateResultDTO result = tenantService.createTenant(tenantSaveDTO);
        return RestResponse.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:update')")
    public RestResponse<TenantDTO> update(@PathVariable Long id, @RequestBody @Valid TenantSaveDTO tenantSaveDTO) {
        tenantSaveDTO.setId(id);
        TenantDTO updatedTenant = tenantService.updateTenant(id, tenantSaveDTO);
        return RestResponse.success(updatedTenant);
    }

    @PutMapping("/{id}/state")
    @PreAuthorize("hasAuthority('tenant:update')")
    public RestResponse<TenantDTO> updateState(@PathVariable Long id,@Valid @NotNull @RequestParam Boolean state) {
        TenantDTO updatedTenant = tenantService.updateTenantState(id, state);
        return RestResponse.success(updatedTenant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:delete')")
    public RestResponse<Void> deleteById(@PathVariable Long id) {
        tenantService.deleteByVerify(id);
        return RestResponse.success();
    }

    @GetMapping("/options")
    public RestResponse<List<TenantOptionsDTO>> getOptions(@RequestParam(required = false) String name) {
        return RestResponse.success(tenantService.findOptions(name));
    }
}