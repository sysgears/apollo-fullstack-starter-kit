package com.sysgears.user.resolvers;

import com.sysgears.user.dto.UserPayload;
import com.sysgears.user.dto.input.FilterUserInput;
import com.sysgears.user.dto.input.OrderByUserInput;
import com.sysgears.user.model.User;
import com.sysgears.user.service.UserService;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserQueryResolver implements GraphQLQueryResolver {
    private final UserService userService;

    public CompletableFuture<User> currentUser() {
        return CompletableFuture.supplyAsync(() -> userService.getCurrentAuditor().orElse(null));
    }

    public CompletableFuture<UserPayload> user(int id) {
        return userService.findUserById(id).thenApply(UserPayload::new);
    }

    public CompletableFuture<List<User>> users(Optional<OrderByUserInput> orderBy, Optional<FilterUserInput> filter) {
        return userService.findByCriteria(orderBy, filter);
    }
}
