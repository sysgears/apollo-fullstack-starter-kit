package com.sysgears.authentication.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionUtils {
    public static final SecurityContext SECURITY_CONTEXT = SecurityContextHolder.createEmptyContext();

}
