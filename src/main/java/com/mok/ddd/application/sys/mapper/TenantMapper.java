package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantOptionDTO;
import com.mok.ddd.domain.sys.model.Tenant;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {

    TenantDTO toDto(Tenant entity);

    void updateEntityFromDto(Tenant entity, @MappingTarget TenantCreateResultDTO dto);

    List<TenantOptionDTO> dtoToOptionsDto(List<TenantDTO> dtoList);
}
