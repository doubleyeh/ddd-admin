package com.mok.ddd.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.mok.ddd.application.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantSaveDTO;
import com.mok.ddd.domain.entity.Tenant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    TenantDTO toDto(Tenant entity);

    Tenant toEntity(TenantDTO dto);

    Tenant toEntity(TenantSaveDTO dto);

    void updateEntityFromDto(TenantSaveDTO dto, @MappingTarget Tenant entity);

    void updateEntityFromDto(Tenant entity, @MappingTarget TenantCreateResultDTO dto);
}