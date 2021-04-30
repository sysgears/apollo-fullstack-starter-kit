package com.sysgears.counter;

import com.sysgears.counter.model.Counter;
import com.sysgears.counter.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterDBInitializer {
    private final CounterRepository repository;

    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent event) {
        long count = repository.count();
        if (count == 0) {
            repository.save(new Counter(1));
            log.debug("Counter initialized");
        }
    }
}