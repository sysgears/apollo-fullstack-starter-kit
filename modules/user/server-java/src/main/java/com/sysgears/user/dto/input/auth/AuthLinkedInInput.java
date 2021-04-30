package com.sysgears.user.dto.input.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthLinkedInInput {
    private String lnId;
    private String displayName;
}
