package com.mok.ddd.application.sys.listener;

import com.mok.ddd.application.sys.dto.user.UserPostDTO;
import com.mok.ddd.application.sys.event.TenantCreatedEvent;
import com.mok.ddd.application.sys.service.UserService;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreationListener {

    private final UserService userService;

    @EventListener
    public void onTenantCreated(TenantCreatedEvent event) {
        Tenant tenant = event.getTenant();
        String rawPassword = event.getRawPassword();

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(Const.DEFAULT_ADMIN_USERNAME);
        userPostDTO.setNickname(tenant.getName() + "管理员");
        userPostDTO.setPassword(rawPassword);
        userPostDTO.setState(Const.UserState.NORMAL);
        userPostDTO.setTenantId(tenant.getTenantId());

        userService.createForTenant(userPostDTO);
    }
}
