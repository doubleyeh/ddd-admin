package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.TenantCreateResultDTO;
import com.mok.ddd.application.dto.TenantDTO;
import com.mok.ddd.application.dto.TenantSaveDTO;
import com.mok.ddd.domain.entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    TenantDTO toDto(Tenant entity);

    Tenant toEntity(TenantDTO dto);

    Tenant toEntity(TenantSaveDTO dto);

    void updateEntityFromDto(TenantSaveDTO dto, @MappingTarget Tenant entity);

    void updateEntityFromDto(Tenant entity, @MappingTarget TenantCreateResultDTO dto);
}