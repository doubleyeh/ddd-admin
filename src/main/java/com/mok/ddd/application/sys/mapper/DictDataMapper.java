package com.mok.ddd.application.sys.mapper;

import com.mok.ddd.application.sys.dto.dict.DictDataDTO;
import com.mok.ddd.application.sys.dto.dict.DictDataSaveDTO;
import com.mok.ddd.domain.sys.model.DictData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictDataMapper {
    DictDataDTO toDto(DictData entity);
    DictData toEntity(DictDataSaveDTO dto);
    void updateEntityFromDto(DictDataSaveDTO dto, @MappingTarget DictData entity);
}
