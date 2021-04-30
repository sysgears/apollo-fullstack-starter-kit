package com.sysgears.user.config;

import com.sysgears.user.model.User;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

@Getter
public class JWTPreAuthenticationToken extends PreAuthenticatedAuthenticationToken {

    public JWTPreAuthenticationToken(User principal, WebAuthenticationDetails details) {
        super(principal, null, principal.getAuthorities());
        super.setDetails(details);
    }
}
