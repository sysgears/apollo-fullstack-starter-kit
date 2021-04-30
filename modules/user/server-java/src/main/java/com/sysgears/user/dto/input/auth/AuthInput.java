package com.sysgears.user.dto.input.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
public class AuthInput {
    private final Optional<AuthCertificateInput> certificate = Optional.empty();
    private final Optional<AuthFacebookInput> facebook = Optional.empty();
    private final Optional<AuthGoogleInput> google = Optional.empty();
    private final Optional<AuthGitHubInput> github = Optional.empty();
    private final Optional<AuthLinkedInInput> linkedin = Optional.empty();
}
