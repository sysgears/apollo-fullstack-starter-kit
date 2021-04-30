package com.sysgears.user.resolvers.password;

import com.sysgears.authentication.model.jwt.Tokens;
import com.sysgears.authentication.service.jwt.JwtGenerator;
import com.sysgears.authentication.service.jwt.JwtParser;
import com.sysgears.authentication.utils.SessionUtils;
import com.sysgears.mailer.service.EmailService;
import com.sysgears.service.MessageResolver;
import com.sysgears.user.config.JWTPreAuthenticationToken;
import com.sysgears.user.dto.AuthPayload;
import com.sysgears.user.dto.UserPayload;
import com.sysgears.user.dto.input.ForgotPasswordInput;
import com.sysgears.user.dto.input.LoginUserInput;
import com.sysgears.user.dto.input.RegisterUserInput;
import com.sysgears.user.dto.input.ResetPasswordInput;
import com.sysgears.user.exception.LoginFailedException;
import com.sysgears.user.exception.ResetPasswordException;
import com.sysgears.user.exception.UserAlreadyExistsException;
import com.sysgears.user.exception.UserNotFoundException;
import com.sysgears.user.model.User;
import com.sysgears.user.service.UserService;
import com.sysgears.user.util.UserIdentityUtils;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginMutationResolver implements GraphQLMutationResolver {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final JwtParser jwtParser;
    private final EmailService emailService;
    private final MessageResolver messageResolver;

    @Transactional(readOnly = true)
    public CompletableFuture<AuthPayload> login(LoginUserInput loginUserInput) {
        return userService.findUserByUsernameOrEmail(loginUserInput.getUsernameOrEmail())
                .thenApply(user -> {
                    boolean matches = passwordEncoder.matches(loginUserInput.getPassword(), user.getPassword());
                    if (!matches) {
                        log.debug("Password is invalid");
                        throw new LoginFailedException(
                                messageResolver.getLocalisedMessage("errors.login.failed"),
                                Map.of("password", messageResolver.getLocalisedMessage("errors.login.invalidPassword"))
                        );
                    }

                    Tokens tokens = jwtGenerator.generateTokens(UserIdentityUtils.convert(user));
                    SessionUtils.SECURITY_CONTEXT.setAuthentication(new JWTPreAuthenticationToken(user, null));

                    return new AuthPayload(user, tokens);
                })
                .exceptionally(throwable -> {
                    if (throwable.getCause() instanceof NoResultException) {
                        log.warn("Specified email or username '{}' is invalid.", loginUserInput.getUsernameOrEmail());
                        throw new LoginFailedException(
                                messageResolver.getLocalisedMessage("errors.login.failed"),
                                Map.of("usernameOrEmail", messageResolver.getLocalisedMessage("errors.login.invalidUsernameOrEmail"))
                        );
                    } else {
                        log.error("Unexpected error happens when find user with " + loginUserInput.getUsernameOrEmail(), throwable);
                        throw (CompletionException) throwable;
                    }
                });
    }

    public CompletableFuture<String> forgotPassword(ForgotPasswordInput input) {
        if (!userService.existsByEmail(input.getEmail())) {
            throw new UserNotFoundException(
                    messageResolver.getLocalisedMessage("errors.userNotExists"),
                    Map.of("email", messageResolver.getLocalisedMessage("errors.forgotPassword.noUserWithEmail"))
            );
        }

        return userService.findUserByUsernameOrEmail(input.getEmail()).thenApply(user -> {
            emailService.sendResetPasswordEmail(
                    input.getEmail(),
                    "/user/reset-password?key=" + jwtGenerator.generateVerificationToken(UserIdentityUtils.convert(user))
            );
            return null;
        });
    }

    public CompletableFuture<String> resetPassword(ResetPasswordInput input) {
        return CompletableFuture.supplyAsync(() -> {
            String token = new String(Base64.getDecoder().decode(input.getToken()));
            return jwtParser.getIdFromVerificationToken(token);
        })
                .thenCompose(userService::findUserById)
                .thenCompose(user -> {
                    if (user == null) {
                        throw new UserNotFoundException(messageResolver.getLocalisedMessage("errors.userNotExists"));
                    }
                    if (!input.getPassword().equals(input.getPasswordConfirmation())) {
                        throw new ResetPasswordException(
                                messageResolver.getLocalisedMessage("errors.resetPassword.failed"),
                                Map.of("passwordConfirmation", messageResolver.getLocalisedMessage("errors.resetPassword.passwordsIsNotMatch"))
                        );
                    }

                    user.setPassword(passwordEncoder.encode(input.getPassword()));
                    userService.save(user);

                    emailService.sendPasswordUpdatedEmail(user.getEmail());

                    return CompletableFuture.completedFuture(null);
                });
    }

    public CompletableFuture<UserPayload> register(RegisterUserInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> errors = new HashMap<>();
            if (userService.existsByEmail(input.getEmail())) {
                errors.put("email", messageResolver.getLocalisedMessage("errors.register.emailIsExisted"));
            }
            if (userService.existsByUsername(input.getUsername())) {
                errors.put("username", messageResolver.getLocalisedMessage("errors.register.usernameIsExisted"));
            }
            if (!errors.isEmpty()) {
                throw new UserAlreadyExistsException(
                        messageResolver.getLocalisedMessage("errors.userAlreadyExists"),
                        errors
                );
            }

            User user = new User(
                    input.getUsername(),
                    passwordEncoder.encode(input.getPassword()),
                    "user",
                    false,
                    input.getEmail());
            User registered = userService.save(user);

            emailService.sendRegistrationConfirmEmail(
                    user.getUsername(),
                    user.getEmail(),
                    "/user/confirm?key=" + jwtGenerator.generateVerificationToken(UserIdentityUtils.convert(registered))
            );

            return new UserPayload(registered);
        });
    }
}
