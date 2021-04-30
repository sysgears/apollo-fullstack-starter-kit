package com.sysgears.user.service;

import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.authentication.resolvers.jwt.JwtUserIdentityService;
import com.sysgears.authentication.service.jwt.JwtParser;
import com.sysgears.authentication.utils.SessionUtils;
import com.sysgears.service.MessageResolver;
import com.sysgears.user.dto.input.FilterUserInput;
import com.sysgears.user.dto.input.OrderByUserInput;
import com.sysgears.user.exception.UserNotFoundException;
import com.sysgears.user.model.User;
import com.sysgears.user.repository.UserRepository;
import com.sysgears.user.util.UserIdentityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService, AuditorAware<User>, JwtUserIdentityService {

	private final UserRepository userRepository;
	private final JwtParser jwtParser;
	private final MessageResolver messageResolver;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return findUserByUsernameOrEmail(username).handle((user, throwable) -> {
			if (throwable != null) {
				if (throwable.getCause() instanceof NoResultException) {
					throw new UsernameNotFoundException(messageResolver.getLocalisedMessage("errors.userWithUsernameNotExists", username));
				}
				log.error(String.format("Unexpected error happened when load user by username '%s'", username), throwable);
			}
			return user;
		}).join();
	}

	@Transactional(readOnly = true)
	public CompletableFuture<User> findUserByUsernameOrEmail(String usernameOrEmail) {
		return userRepository.findByUsernameOrEmail(usernameOrEmail);
	}

	@NonNull
	@Override
	public Optional<User> getCurrentAuditor() {
		final Authentication authentication = SessionUtils.SECURITY_CONTEXT.getAuthentication();

		if (authentication == null) {
			// no user
			return Optional.empty();
		}

		if (authentication.getPrincipal() instanceof User) {
			return Optional.of((User) SessionUtils.SECURITY_CONTEXT.getAuthentication().getPrincipal());
		} else {
			// anonymous user
			return Optional.empty();
		}
	}

	@Transactional(readOnly = true)
	public User loadUserByToken(String token) {
		Integer userId = jwtParser.getIdFromAccessToken(token);
		return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(messageResolver.getLocalisedMessage("errors.userNotExists")));
	}

	@Transactional(readOnly = true)
	public CompletableFuture<List<User>> findByCriteria(Optional<OrderByUserInput> orderBy, Optional<FilterUserInput> filter) {
		return userRepository.findByCriteria(orderBy, filter);
	}

	public User save(User user) {
		return userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public CompletableFuture<User> findUserById(Integer id) {
		return userRepository.findUserById(id);
	}

	public void delete(User user) {
		userRepository.delete(user);
	}

	@Override
	public Optional<JwtUserIdentity> findById(Integer userId) {
		return userRepository.findById(userId).map(UserIdentityUtils::convert);
	}

	@Transactional(readOnly = true)
	public Boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public Boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}
}
