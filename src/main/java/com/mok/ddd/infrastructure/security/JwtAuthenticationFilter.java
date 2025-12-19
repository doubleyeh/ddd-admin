package com.mok.ddd.infrastructure.security;

import com.mok.ddd.application.service.PermissionService;
import com.mok.ddd.common.Const;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final PermissionService permissionService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, PermissionService permissionService) {
        this.tokenProvider = tokenProvider;
        this.permissionService = permissionService;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt)) {
            TokenSessionDTO session = tokenProvider.getSession(jwt);

            if (session != null) {
                String tenantId = session.getTenantId();
                String username = session.getUsername();
                CustomUserDetail principal = session.getPrincipal();

                Set<Long> roleIds = principal.getRoleIds();
                List<SimpleGrantedAuthority> authorities;
                if(null == roleIds || roleIds.isEmpty()){
                    authorities = Collections.emptyList();
                }
                // 超管
                else if(roleIds.contains(0L)){
                    Set<String> permissionCodes = permissionService.getAllPermissionCodes();
                    authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(Const.SUPER_ADMIN_ROLE_CODE));
                    authorities.addAll(permissionCodes.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList());
                }else{
                    Set<String> permissionCodes = permissionService.getPermissionsByRoleIds(roleIds);
                    authorities = permissionCodes.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                        .where(TenantContextHolder.USERNAME, username)
                        .run(() -> {
                            try {
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                filterChain.doFilter(request, response);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                SecurityContextHolder.clearContext();
                            }
                        });
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}