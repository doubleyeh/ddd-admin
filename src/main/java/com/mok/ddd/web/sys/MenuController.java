package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.menu.MenuOptionDTO;
import com.mok.ddd.application.sys.service.MenuService;
import com.mok.ddd.web.common.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<List<MenuDTO>> getTree() {
        return RestResponse.success(menuService.buildMenuTree(menuService.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<MenuDTO> save(@RequestBody MenuDTO dto) {
        return RestResponse.success(menuService.createMenu(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<MenuDTO> update(@PathVariable Long id, @RequestBody MenuDTO dto) {
        dto.setId(id);
        return RestResponse.success(menuService.updateMenu(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RestResponse<Void> delete(@PathVariable Long id) {
        menuService.deleteById(id);
        return RestResponse.success();
    }

    @GetMapping("/tree-options")
    public RestResponse<List<MenuOptionDTO>> getMenuTreeOptions() {
        return RestResponse.success(menuService.buildMenuAndPermissionTree());
    }
}