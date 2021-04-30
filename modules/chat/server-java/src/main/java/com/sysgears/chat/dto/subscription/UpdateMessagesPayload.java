package com.sysgears.chat.dto.subscription;

import com.sysgears.chat.dto.MessagePayload;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class UpdateMessagesPayload {
    @NonNull
    private final String mutation;
    private final Integer id;
    @NonNull
    private final MessagePayload node;
}
