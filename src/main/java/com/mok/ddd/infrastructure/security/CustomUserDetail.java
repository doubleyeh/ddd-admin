package com.mok.ddd.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Getter
public class CustomUserDetail implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String tenantId;
    private final Set<Long> roleIds;
    private Collection<? extends GrantedAuthority> authorities;
    @JsonProperty("superAdmin")
    private final boolean isSuperAdmin;

    public CustomUserDetail(Long userId, String username, String password, String tenantId, Set<Long> roleIds, boolean isSuperAdmin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.tenantId = tenantId;
        this.roleIds = roleIds;
        this.isSuperAdmin = isSuperAdmin;
        this.authorities = Collections.emptyList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}