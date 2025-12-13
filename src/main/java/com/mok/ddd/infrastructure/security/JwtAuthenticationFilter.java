package com.mok.ddd.infrastructure.security;

import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,@Nonnull HttpServletResponse response,@Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        String currentTenantId = null;
        String currentUsername = null;
        UsernamePasswordAuthenticationToken authentication = null;

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                Claims claims = tokenProvider.validateAndGetClaims(jwt);

                if (claims != null) {
                    currentUsername = claims.getSubject();
                    currentTenantId = tokenProvider.getTenantIdFromClaims(claims);

                    if (currentTenantId != null) {
                        String finalCurrentUsername = currentUsername;
                        UserDetails userDetails = ScopedValue.where(TenantContextHolder.TENANT_ID, currentTenantId)
                                .where(TenantContextHolder.USERNAME, currentUsername)
                                .call(() -> customUserDetailsService.loadUserByUsername(finalCurrentUsername));

                        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (currentTenantId != null && currentUsername != null && authentication != null) {
            UsernamePasswordAuthenticationToken finalAuthentication = authentication;
            ScopedValue.where(TenantContextHolder.TENANT_ID, currentTenantId)
                    .where(TenantContextHolder.USERNAME, currentUsername)
                    .run(() -> {
                        try {
                            SecurityContextHolder.getContext().setAuthentication(finalAuthentication);
                            filterChain.doFilter(request, response);
                        } catch (IOException | ServletException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}