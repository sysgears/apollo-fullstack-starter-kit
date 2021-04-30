package com.sysgears.post.subscription;

import com.sysgears.core.subscription.AbstractPubSubService;
import org.springframework.stereotype.Component;

@Component
public class PostPubSubService extends AbstractPubSubService<PostUpdatedEvent> {
}
