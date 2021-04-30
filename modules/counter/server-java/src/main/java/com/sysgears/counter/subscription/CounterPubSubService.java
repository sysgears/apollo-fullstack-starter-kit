package com.sysgears.counter.subscription;

import com.sysgears.core.subscription.AbstractPubSubService;
import com.sysgears.counter.model.Counter;
import org.springframework.stereotype.Component;

@Component
public class CounterPubSubService extends AbstractPubSubService<Counter> {
}
