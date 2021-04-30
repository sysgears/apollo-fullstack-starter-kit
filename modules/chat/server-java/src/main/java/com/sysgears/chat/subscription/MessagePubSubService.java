package com.sysgears.chat.subscription;

import com.sysgears.core.subscription.AbstractPubSubService;
import org.springframework.stereotype.Component;

@Component
public class MessagePubSubService extends AbstractPubSubService<MessageUpdatedEvent> {
}
