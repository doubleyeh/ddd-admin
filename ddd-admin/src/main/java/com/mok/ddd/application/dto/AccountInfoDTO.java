package com.mok.ddd.application.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
@Builder
public class AccountInfoDTO implements Serializable {
    UserDTO user;

    Collection<MenuDTO> menus;

    Collection<String> permissions;
}
