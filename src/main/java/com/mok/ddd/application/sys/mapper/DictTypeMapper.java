package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.dict.DictTypeDTO;
import com.mok.ddd.domain.sys.model.DictType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictTypeMapper {
    DictTypeDTO toDto(DictType entity);
}
