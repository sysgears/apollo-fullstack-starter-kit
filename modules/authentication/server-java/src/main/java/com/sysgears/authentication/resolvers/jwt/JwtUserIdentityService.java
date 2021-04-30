package com.sysgears.authentication.resolvers.jwt;

import com.sysgears.authentication.model.jwt.JwtUserIdentity;

import java.util.Optional;

public interface JwtUserIdentityService {
    Optional<JwtUserIdentity> findById(Integer id);
}
