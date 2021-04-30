package com.sysgears.authentication.service.jwt;

import com.sysgears.authentication.config.JwtConfig;
import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.authentication.model.jwt.Tokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService implements JwtGenerator, JwtParser {
    private final Key secretKey;
    private final JwtConfig jwtConfig;

    public Tokens generateTokens(JwtUserIdentity identity) {
        return new Tokens(generateAccessToken(identity), generateRefreshToken(identity));
    }

    public String generateVerificationToken(JwtUserIdentity identity) {
        return Jwts.builder()
                .setSubject(String.valueOf(identity.getId()))
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getAccessTokenExpirationInSec(), ChronoUnit.SECONDS)))
                .setHeaderParam("typ", "JWT")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Integer getIdFromAccessToken(String token) {
        return (Integer) Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("identity", Map.class)
                .get("id");
    }

    public Integer getIdFromRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Integer.class);
    }

    public Integer getIdFromVerificationToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Integer.parseInt(claims.getSubject());
    }

    private String generateAccessToken(JwtUserIdentity identity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("identity", identity);
        log.debug("Generating new access JWT for user {}", identity.getId());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getAccessTokenExpirationInSec(), ChronoUnit.SECONDS)))
                .setHeaderParam("typ", "JWT")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(JwtUserIdentity identity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", identity.getId());
        log.debug("Generating new refresh JWT for user {}", identity.getId());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getRefreshTokenExpirationInSec(), ChronoUnit.SECONDS)))
                .setHeaderParam("typ", "JWT")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
