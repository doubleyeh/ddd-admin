package com.mok.ddd.application.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class AccountInfoDTO implements Serializable {
    private UserDTO user;
    private List<String> permissions;
    private List<MenuDTO> menus;
}