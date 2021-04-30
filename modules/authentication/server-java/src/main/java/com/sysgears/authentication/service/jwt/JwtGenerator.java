package com.sysgears.authentication.service.jwt;

import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.authentication.model.jwt.Tokens;

public interface JwtGenerator {
    Tokens generateTokens(JwtUserIdentity identity);

    String generateVerificationToken(JwtUserIdentity identity);
}
