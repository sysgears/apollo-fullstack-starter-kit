package com.sysgears.chat.subscription;

import com.sysgears.chat.dto.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageUpdatedEvent {
	private Mutation mutation;
	private MessagePayload message;
}
