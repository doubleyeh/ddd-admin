package com.mok.ddd.application.dto.user;

import com.mok.ddd.application.dto.role.RoleOptionsDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private Integer state;
    private LocalDateTime createTime;

    private String tenantId;
    private String tenantName;

    private Set<RoleOptionsDTO> roles;
}