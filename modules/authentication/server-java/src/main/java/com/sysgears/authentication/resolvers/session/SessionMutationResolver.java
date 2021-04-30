package com.sysgears.authentication.resolvers.session;

import com.sysgears.authentication.utils.SessionUtils;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionMutationResolver implements GraphQLMutationResolver {

    public String logout() {
       if (SessionUtils.SECURITY_CONTEXT.getAuthentication() != null) {
           log.info("Logout user '{}'", SessionUtils.SECURITY_CONTEXT.getAuthentication().getName());
           SessionUtils.SECURITY_CONTEXT.setAuthentication(null);
       }
        return null;
    }
}
