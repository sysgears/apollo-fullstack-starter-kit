package com.sysgears.user.util;

import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.user.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserIdentityUtils {

    public static JwtUserIdentity convert(User user) {
        JwtUserIdentity.JwtUserIdentityBuilder builder = JwtUserIdentity.builder();
        builder
                .id(user.getId())
                .username(user.getUsername())
                .passwordHash(user.getPassword())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .email(user.getEmail());

        if (user.getProfile() != null) {
            builder
                    .firstName(user.getProfile().getFirstName())
                    .lastName(user.getProfile().getLastName());
        }
        return builder.build();
    }
}
