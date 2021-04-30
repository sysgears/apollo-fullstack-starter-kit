package com.sysgears.counter.resolvers;

import com.sysgears.counter.constant.CounterConstants;
import com.sysgears.counter.model.Counter;
import com.sysgears.counter.repository.CounterRepository;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterQueryResolver implements GraphQLQueryResolver {
    private final CounterRepository repository;

    public CompletableFuture<Counter> serverCounter() {
        log.debug("Get counter");
        return repository.findById(CounterConstants.ID);
    }
}
