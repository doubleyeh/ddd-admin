package com.mok.ddd.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.domain.entity.Menu;
import com.mok.ddd.domain.repository.MenuRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService extends BaseServiceImpl<Menu, Long, MenuDTO> {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    public List<MenuDTO> buildMenuTree(List<MenuDTO> flatList) {
        Map<Long, MenuDTO> dtoMap = flatList.stream()
                .collect(Collectors.toMap(MenuDTO::getId, dto -> dto));

        List<MenuDTO> rootMenus = new ArrayList<>();

        for (MenuDTO dto : flatList) {
            if (Boolean.TRUE.equals(dto.getIsHidden())) {
                continue;
            }

            if (dto.getParentId() == null || dto.getParentId() == 0) {
                rootMenus.add(dto);
            } else {
                MenuDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }

        return filterEmptyParentMenus(rootMenus);
    }

    private List<MenuDTO> filterEmptyParentMenus(List<MenuDTO> menus) {
        List<MenuDTO> filtered = new ArrayList<>();
        for (MenuDTO menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                List<MenuDTO> filteredChildren = filterEmptyParentMenus(menu.getChildren());
                menu.setChildren(filteredChildren);
            }

            if ((menu.getChildren() == null || menu.getChildren().isEmpty()) && menu.getPath() != null
                    && !menu.getPath().isEmpty()) {
                filtered.add(menu);
            } else if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                filtered.add(menu);
            }
        }
        return filtered;
    }

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
