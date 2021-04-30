package com.sysgears.user.dto.input.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthFacebookInput {
    private String fbId;
    private String displayName;
}
