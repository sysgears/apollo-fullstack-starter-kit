package com.sysgears.user.dto.input;

import com.sysgears.user.dto.input.auth.AuthInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditUserInput {
    @NonNull
    private int id;
    @NonNull
    private String username;
    @NonNull
    private String role;
    @NonNull
    private String email;
    private final Optional<Boolean> isActive = Optional.empty();
    private final Optional<String> password = Optional.empty();
    private final Optional<ProfileInput> profile = Optional.empty();
    private final Optional<AuthInput> auth = Optional.empty();
}
