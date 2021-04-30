package com.sysgears.user.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
public class FilterUserInput {
    private final Optional<String> searchText = Optional.empty();
    private final Optional<String> role = Optional.empty();
    private final Optional<Boolean> isActive = Optional.empty();
}
