package com.mok.ddd.application.sys.dto.auth;

import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoDTO implements Serializable {
    UserDTO user;

    Collection<MenuDTO> menus;

    Collection<String> permissions;
}