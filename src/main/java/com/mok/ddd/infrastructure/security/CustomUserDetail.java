package com.mok.ddd.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetail implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String tenantId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isSuperAdmin;

    public CustomUserDetail(Long userId, String username, String password, String tenantId, Collection<? extends GrantedAuthority> authorities, boolean isSuperAdmin) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.tenantId = tenantId;
        this.authorities = authorities;
        this.isSuperAdmin = isSuperAdmin;
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