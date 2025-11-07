package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.MenuDTO;
import com.mok.ddd.domain.entity.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuMapper {

    MenuDTO toDto(Menu entity);

    Menu toEntity(MenuDTO dto);
}