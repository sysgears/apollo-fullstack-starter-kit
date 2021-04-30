package com.sysgears.user.subscription;

import com.sysgears.core.subscription.AbstractPubSubService;
import org.springframework.stereotype.Component;

@Component
public class UserPubSubService extends AbstractPubSubService<UserUpdatedEvent> {
}
