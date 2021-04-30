package com.sysgears.user.dto.input;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
public class ProfileInput {
    private final Optional<String> firstName = Optional.empty();
    private final Optional<String> lastName = Optional.empty();
}
