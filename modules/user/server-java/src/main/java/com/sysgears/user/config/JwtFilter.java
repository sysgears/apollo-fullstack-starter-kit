package com.sysgears.user.config;

import com.sysgears.authentication.utils.SessionUtils;
import com.sysgears.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Pattern BEARER_PATTERN = Pattern.compile("^Bearer (.+?)$");
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        getToken(request)
                .map(userService::loadUserByToken)
                .map(user -> new JWTPreAuthenticationToken(user, new WebAuthenticationDetailsSource().buildDetails(request)))
                .ifPresent(SessionUtils.SECURITY_CONTEXT::setAuthentication);
        filterChain.doFilter(request, response);
    }

    private Optional<String> getToken(HttpServletRequest request) {
        return Optional
                .ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(Predicate.not(String::isEmpty))
                .map(BEARER_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1));
    }
}
