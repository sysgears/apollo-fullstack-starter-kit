package com.sysgears.counter.resolvers;

import com.sysgears.core.subscription.Publisher;
import com.sysgears.counter.constant.CounterConstants;
import com.sysgears.counter.model.Counter;
import com.sysgears.counter.repository.CounterRepository;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterMutationResolver implements GraphQLMutationResolver {
    private final CounterRepository repository;
    private final Publisher<Counter> publisher;

    public CompletableFuture<Counter> addServerCounter(int amount) {
        log.debug("Updating counter");
        return repository.findById(CounterConstants.ID)
                .thenApply(counter -> {
                            counter.increaseAmount(amount);
                            repository.save(counter);
                            publisher.publish(counter);
                            return counter;
                        }
                );
    }
}
