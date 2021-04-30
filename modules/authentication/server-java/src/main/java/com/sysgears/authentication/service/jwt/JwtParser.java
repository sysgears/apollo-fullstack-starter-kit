package com.sysgears.authentication.service.jwt;

public interface JwtParser {
    Integer getIdFromAccessToken(String token);

    Integer getIdFromRefreshToken(String token);

    Integer getIdFromVerificationToken(String token);
}
