package com.sysgears.user.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordInput {
    @NonNull
    private String token;
    @NonNull
    private String password;
    @NonNull
    private String passwordConfirmation;
}
