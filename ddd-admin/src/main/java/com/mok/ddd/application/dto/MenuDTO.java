package com.mok.ddd.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class MenuDTO {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Boolean isHidden;
    private List<MenuDTO> children;
}