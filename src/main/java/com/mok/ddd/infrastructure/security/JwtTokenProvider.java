package com.mok.ddd.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public String getUsernameFromClaims(Claims claims) {
        return claims.getSubject();
    }

    public String getTenantIdFromClaims(Claims claims) {
        return claims.get("tenantId", String.class);
    }

    public String createToken(String username, String tenantId) {
        long nowMillis = System.currentTimeMillis();

        return Jwts.builder()
                .claim("sub", username)
                .claim("tenantId", tenantId)
                .claim("iat", nowMillis / 1000)
                .claim("exp", (nowMillis + jwtExpirationInMs) / 1000)
                .signWith(this.key)
                .compact();
    }
}