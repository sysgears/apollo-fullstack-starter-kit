package com.sysgears.authentication.resolvers.jwt;

import com.sysgears.authentication.exception.RefreshTokenInvalidException;
import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.authentication.model.jwt.Tokens;
import com.sysgears.authentication.service.jwt.JwtGenerator;
import com.sysgears.authentication.service.jwt.JwtParser;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class JwtMutationResolver implements GraphQLMutationResolver {
    private final JwtParser jwtParser;
    private final JwtGenerator jwtGenerator;
    private final JwtUserIdentityService userIdentityService;

    public CompletableFuture<Tokens> refreshTokens(String refreshToken) {
        return CompletableFuture.supplyAsync(() -> {
            if (refreshToken.isBlank()) {
                throw new IllegalArgumentException();
            }
            Integer userId = jwtParser.getIdFromRefreshToken(refreshToken);
            JwtUserIdentity userIdentity = userIdentityService.findById(userId).orElseThrow(RefreshTokenInvalidException::new);
            return jwtGenerator.generateTokens(userIdentity);
        });
    }
}
