package com.sysgears.counter.resolvers;

import com.sysgears.core.subscription.Subscriber;
import com.sysgears.counter.model.Counter;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSubscriptionResolver implements GraphQLSubscriptionResolver {
    private final Subscriber<Counter> subscriber;

    public Publisher<Counter> counterUpdated() {
        log.debug("Subscribing counter updates");
        return subscriber.subscribe(o -> true);
    }
}
