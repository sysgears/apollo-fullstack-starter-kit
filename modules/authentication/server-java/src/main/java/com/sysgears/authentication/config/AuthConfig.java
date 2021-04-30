package com.sysgears.authentication.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;

@Configuration
public class AuthConfig {
    @Bean
    public Key jwtSecretKey(JwtConfig config) {
        return Keys.hmacShaKeyFor(config.getSecret().getBytes());
    }
}
