package com.mok.ddd.domain.sys.model;

import com.mok.ddd.common.Const;
import com.mok.ddd.domain.common.model.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Set;

@Entity
@Table(name = "sys_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TenantBaseEntity {
    private String username;
    private String password;
    private String nickname;
    /**
     * 1正常， 0禁用
     */
    private Integer state;

    /**
     * 是否为租户管理员（仅租户创建时初始化的用户为true）
     */
    @Column(name = "is_tenant_admin")
    private Boolean isTenantAdmin = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    public static User create(@NonNull String username, @NonNull String password, String nickname, boolean isTenantAdmin) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.nickname = nickname;
        user.isTenantAdmin = isTenantAdmin;
        user.state = Const.UserState.NORMAL;
        return user;
    }

    public void assignTenant(String tenantId) {
        if (this.getTenantId() == null) {
            this.setTenantId(tenantId);
        }
    }

    public void updateInfo(String nickname, Set<Role> roles) {
        this.nickname = nickname;
        this.roles = roles;
    }

    public void changePassword(@NonNull String newPassword) {
        this.password = newPassword;
    }

    public void disable() {
        this.state = Const.UserState.DISABLED;
    }

    public void enable() {
        this.state = Const.UserState.NORMAL;
    }

    public void changeRoles(Set<Role> newRoles) {
        this.roles = newRoles;
    }
}
