package com.mok.ddd.application.mapper;

import com.mok.ddd.application.dto.MenuDTO;
import com.mok.ddd.domain.entity.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuMapper {

    MenuDTO toDto(Menu entity);

    Menu toEntity(MenuDTO dto);

    default List<MenuDTO> toDtoList(Collection<Menu> list){
        if(list == null){
            return Collections.emptyList();
        }

        return list.stream().map(this::toDto).toList();
    }
}