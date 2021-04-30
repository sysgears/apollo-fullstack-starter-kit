package com.sysgears.user.resolvers;

import com.sysgears.core.subscription.Subscriber;
import com.sysgears.user.dto.UpdateUserPayload;
import com.sysgears.user.dto.input.FilterUserInput;
import com.sysgears.user.subscription.UserUpdatedEvent;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSubscriptionResolver implements GraphQLSubscriptionResolver {
    private final Subscriber<UserUpdatedEvent> subscriber;

    public CompletableFuture<Publisher<UpdateUserPayload>> usersUpdated(Optional<FilterUserInput> filter) {
        return CompletableFuture.supplyAsync(() -> subscriber.subscribe(event -> {
            if (filter.isEmpty()) return true;

            return switch (event.getMutation()) {
                case DELETE_USER -> true;
                case ADD_USER, EDIT_USER -> filter.get().getIsActive().map(isActive ->
                        event.getUser().getIsActive().equals(isActive)).orElse(true) &&
                        filter.get().getRole().map(role ->
                                event.getUser().getRole().equalsIgnoreCase(role)).orElse(true) &&
                        (filter.get().getSearchText().map(searchText ->
                                event.getUser().getEmail().equalsIgnoreCase(searchText)).orElse(true) ||
                                filter.get().getSearchText().map(searchText ->
                                        event.getUser().getUsername().equalsIgnoreCase(searchText)).orElse(true));
                default -> false;
            };
        }).map(event -> new UpdateUserPayload(event.getMutation().getOperation(), event.getUser())));
    }
}
