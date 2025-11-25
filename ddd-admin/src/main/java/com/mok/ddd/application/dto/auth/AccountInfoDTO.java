package com.mok.ddd.application.dto.auth;

import java.io.Serializable;
import java.util.Collection;

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.user.UserDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountInfoDTO implements Serializable {
    UserDTO user;

    Collection<MenuDTO> menus;

    Collection<String> permissions;
}
