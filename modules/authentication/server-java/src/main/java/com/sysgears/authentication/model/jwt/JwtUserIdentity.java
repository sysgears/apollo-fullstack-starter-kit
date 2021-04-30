package com.sysgears.authentication.model.jwt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtUserIdentity {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String role;
    private final Boolean isActive;
    private final String email;
    private final String firstName;
    private final String lastName;
}
