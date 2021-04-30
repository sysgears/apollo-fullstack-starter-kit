package com.sysgears.user.dto.input;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
public class OrderByUserInput {
    private final Optional<String> column = Optional.empty();
    private final Optional<String> order = Optional.empty();
}
