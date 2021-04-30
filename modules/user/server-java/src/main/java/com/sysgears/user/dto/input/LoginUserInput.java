package com.sysgears.user.dto.input;

import lombok.*;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserInput {
    @NonNull
    private String usernameOrEmail;
    @NonNull
    private String password;
}
