package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.dto.tenant.TenantSaveDTO;
import com.mok.ddd.domain.entity.Tenant;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    TenantDTO toDto(Tenant entity);

    Tenant toEntity(TenantDTO dto);

    Tenant toEntity(TenantSaveDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "packageId", source = "packageId")
    void updateEntityFromDto(TenantSaveDTO dto, @MappingTarget Tenant entity);

    void updateEntityFromDto(Tenant entity, @MappingTarget TenantCreateResultDTO dto);

    List<TenantOptionDTO> dtoToOptionsDto(List<TenantDTO> dtoList);
}
