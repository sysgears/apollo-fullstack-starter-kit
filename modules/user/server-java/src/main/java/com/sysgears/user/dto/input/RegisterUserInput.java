package com.sysgears.user.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserInput {
    @NonNull
    private String username;
    @NonNull
    private String email;
    @NonNull
    private String password;
}
