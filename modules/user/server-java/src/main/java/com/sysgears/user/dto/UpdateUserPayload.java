package com.sysgears.user.dto;

import com.sysgears.user.model.User;
import lombok.Data;

@Data
public class UpdateUserPayload {
    private final String mutation;
    private final User node;
}
