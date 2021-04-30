package com.sysgears.user.resolvers;

import com.sysgears.authentication.utils.SessionUtils;
import com.sysgears.core.subscription.Publisher;
import com.sysgears.service.MessageResolver;
import com.sysgears.user.dto.UserPayload;
import com.sysgears.user.dto.input.AddUserInput;
import com.sysgears.user.dto.input.EditUserInput;
import com.sysgears.user.dto.input.ProfileInput;
import com.sysgears.user.dto.input.auth.AuthInput;
import com.sysgears.user.exception.UserDeletionException;
import com.sysgears.user.exception.UserNotFoundException;
import com.sysgears.user.model.User;
import com.sysgears.user.model.UserAuth;
import com.sysgears.user.model.UserProfile;
import com.sysgears.user.model.auth.*;
import com.sysgears.user.service.UserService;
import com.sysgears.user.subscription.UserUpdatedEvent;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMutationResolver implements GraphQLMutationResolver {
	private final UserService userService;
	private final Publisher<UserUpdatedEvent> publisher;
	private final PasswordEncoder passwordEncoder;
	private final MessageResolver messageResolver;

	public CompletableFuture<UserPayload> addUser(AddUserInput input) {
		return CompletableFuture.supplyAsync(() -> {
			User user = new User(
					input.getUsername(),
					passwordEncoder.encode(input.getPassword()),
					input.getRole(),
					input.getIsActive(),
					input.getEmail()
			);
			input.getProfile().map(profileInput ->
					new UserProfile(
							profileInput.getFirstName().orElse(""),
							profileInput.getLastName().orElse("")
					)
			).ifPresent(user::setProfile);

			input.getAuth()
					.map(this::from)
					.ifPresent(user::setAuth);
			userService.save(user);

			publisher.publish(new UserUpdatedEvent(UserUpdatedEvent.Mutation.ADD_USER, user));
			return new UserPayload(user);
		});
	}

	public CompletableFuture<UserPayload> editUser(EditUserInput input) {
		return userService.findUserById(input.getId())
				.thenApplyAsync(user -> {
					if (user == null)
						throw new UserNotFoundException(messageResolver.getLocalisedMessage("errors.userWithIdNotExists", input.getId()));

					user.setUsername(input.getUsername());
					user.setRole(input.getRole());
					user.setEmail(input.getEmail());

					input.getIsActive().ifPresent(user::setIsActive);
					input.getPassword().ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));

					if (user.getProfile() != null) {
						input.getProfile().ifPresent(profile -> {
							profile.getFirstName().ifPresent(fn -> user.getProfile().setFirstName(fn));
							profile.getLastName().ifPresent(ln -> user.getProfile().setLastName(ln));

							user.getProfile().setFullName(
									user.getProfile().getFirstName()
											.concat(" ")
											.concat(user.getProfile().getLastName())
							);
						});
					} else {
						input.getProfile().map(this::from).ifPresent(user::setProfile);
					}

					if (user.getAuth() != null) {
						input.getAuth().ifPresent(authInput -> {
							authInput.getCertificate().ifPresent(cert ->
									user.getAuth().setCertificate(new CertificateAuth(cert.getSerial())));
							authInput.getFacebook().ifPresent(fb ->
									user.getAuth().setFacebook(new FacebookAuth(fb.getFbId(), fb.getDisplayName())));
							authInput.getLinkedin().ifPresent(li ->
									user.getAuth().setLinkedin(new LinkedInAuth(li.getLnId(), li.getDisplayName())));
							authInput.getGoogle().ifPresent(g ->
									user.getAuth().setGoogle(new GoogleAuth(g.getGoogleId(), g.getDisplayName())));
							authInput.getGithub().ifPresent(git ->
									user.getAuth().setGithub(new GithubAuth(git.getGhId(), git.getDisplayName())));
						});
					} else {
						input.getAuth().map(this::from).ifPresent(user::setAuth);
					}

					userService.save(user);

					publisher.publish(new UserUpdatedEvent(UserUpdatedEvent.Mutation.EDIT_USER, user));

					return new UserPayload(user);
				});
	}

	public CompletableFuture<UserPayload> deleteUser(int id) {
		return userService.findUserById(id).thenApplyAsync(user -> {
			if (user == null) {
				throw new UserNotFoundException(messageResolver.getLocalisedMessage("errors.userWithIdNotExists", id));
			}

			Authentication authentication = SessionUtils.SECURITY_CONTEXT.getAuthentication();
			if (authentication == null) {
				throw new UserDeletionException(messageResolver.getLocalisedMessage("errors.deleteUser.notEnoughPermission"));
			} else if (!((User) authentication.getPrincipal()).getRole().equals("admin")) {
				throw new UserDeletionException(messageResolver.getLocalisedMessage("errors.deleteUser.notEnoughPermission"));
			} else if (((User) authentication.getPrincipal()).getId() == user.getId()) {
				throw new UserDeletionException(messageResolver.getLocalisedMessage("errors.deleteUser.cannotDeleteYourself"));
			}

			userService.delete(user);

			publisher.publish(new UserUpdatedEvent(UserUpdatedEvent.Mutation.DELETE_USER, user));

			return new UserPayload(user);
		});
	}

	private UserAuth from(AuthInput authInput) {
		UserAuth.UserAuthBuilder builder = UserAuth.builder();
		authInput.getCertificate()
				.ifPresent(cert -> builder.certificate(new CertificateAuth(cert.getSerial())));
		authInput.getFacebook()
				.ifPresent(fb -> builder.facebook(new FacebookAuth(fb.getFbId(), fb.getDisplayName())));
		authInput.getGithub()
				.ifPresent(git -> builder.github(new GithubAuth(git.getGhId(), git.getDisplayName())));
		authInput.getGoogle()
				.ifPresent(g -> builder.google(new GoogleAuth(g.getGoogleId(), g.getDisplayName())));
		authInput.getLinkedin()
				.ifPresent(li -> builder.linkedin(new LinkedInAuth(li.getLnId(), li.getDisplayName())));
		return builder.build();
	}

	private UserProfile from(ProfileInput input) {
		return new UserProfile(
				input.getFirstName().orElse(""),
				input.getLastName().orElse("")
		);
	}
}
