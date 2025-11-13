package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.MenuDTO;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.domain.entity.Menu;
import com.mok.ddd.domain.repository.MenuRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService extends BaseServiceImpl<Menu, Long, MenuDTO> {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    @Override
    protected CustomRepository<Menu, Long> getRepository() {
        return menuRepository;
    }

    @Override
    protected Menu toEntity(MenuDTO menuDTO) {
        return menuMapper.toEntity(menuDTO);
    }

    @Override
    protected MenuDTO toDto(Menu entity) {
        return menuMapper.toDto(entity);
    }
}
