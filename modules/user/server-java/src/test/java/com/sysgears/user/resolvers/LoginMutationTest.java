package com.sysgears.user.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.authentication.service.jwt.JwtParser;
import com.sysgears.mailer.service.EmailService;
import com.sysgears.user.dto.AuthPayload;
import com.sysgears.user.model.User;
import com.sysgears.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class LoginMutationTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@MockBean
	private EmailService emailService;
	@SpyBean
	private JwtParser jwtParser;

	private User user;
	private User admin;

	@BeforeEach
	void init() {
		user = userRepository.findByUsernameOrEmail("user").join();
		admin = userRepository.findByUsernameOrEmail("admin").join();
	}

	@Test
	void login_with_username() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("usernameOrEmail", user.getUsername());
		node.put("password", "user1234");

		input.set("input", node);
		GraphQLResponse response = template.perform("mutation/login.graphql", input);

		assertTrue(response.isOk());
		AuthPayload payload = response.get("$.data.login", AuthPayload.class);

		User userActual = payload.getUser();
		assertEquals(user.getUsername(), userActual.getUsername());
		assertEquals(user.getRole(), userActual.getRole());
		assertTrue(userActual.getIsActive());
		assertEquals(user.getEmail(), userActual.getEmail());
		assertNull(userActual.getProfile());

		assertNotNull(payload.getTokens().getAccessToken());
		assertNotNull(payload.getTokens().getRefreshToken());
	}

	@Test
	void login_with_email() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("usernameOrEmail", admin.getEmail());
		node.put("password", "admin123");

		input.set("input", node);
		GraphQLResponse response = template.perform("mutation/login.graphql", input);

		assertTrue(response.isOk());
		AuthPayload payload = response.get("$.data.login", AuthPayload.class);

		User userActual = payload.getUser();
		assertEquals(admin.getUsername(), userActual.getUsername());
		assertEquals(admin.getRole(), userActual.getRole());
		assertTrue(userActual.getIsActive());
		assertEquals(admin.getEmail(), userActual.getEmail());
		assertNull(userActual.getProfile());

		assertNotNull(payload.getTokens().getAccessToken());
		assertNotNull(payload.getTokens().getRefreshToken());
	}

	@Test
	void login_invalid_email() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("usernameOrEmail", "invalid_user");
		node.put("password", "supersecret");

		input.set("input", node);
		GraphQLResponse response = template.perform("mutation/login.graphql", input);

		assertTrue(response.isOk());
		assertEquals("Login failed.", response.get("$.errors[0].message"));
		assertEquals("Please enter a valid username or e-mail.", response.get("$.errors[0].extensions.exception.errors.usernameOrEmail"));
	}

	@Test
	void login_invalid_password() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("usernameOrEmail", admin.getUsername());
		node.put("password", "supersecret");

		input.set("input", node);

		GraphQLResponse response = template.perform("mutation/login.graphql", input);

		assertTrue(response.isOk());
		assertEquals("Login failed.", response.get("$.errors[0].message"));
		assertEquals("Please enter a valid password.", response.get("$.errors[0].extensions.exception.errors.password"));
	}

	@Test
	void forgotPassword() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("email", user.getEmail());
		input.set("input", node);

		GraphQLResponse response = template.perform("/mutation/forgot-password.graphql", input);

		assertTrue(response.isOk());

		verify(emailService).sendResetPasswordEmail(eq(user.getEmail()), startsWith("/user/reset-password?key="));
	}

	@Test
	void forgotPassword_invalid_email() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("email", "user+wrong@example.com");
		input.set("input", node);

		GraphQLResponse response = template.perform("/mutation/forgot-password.graphql", input);

		assertTrue(response.isOk());
		assertEquals("User does not exist.", response.get("$.errors[0].message"));
		assertEquals("No user with specified email.", response.get("$.errors[0].extensions.exception.errors.email"));

		verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
	}

	@Test
	@Transactional
	@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
	void resetPassword() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String token = "some.verification.token";
		String password = "newpassword";

		node.put("token", Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)));
		node.put("password", password);
		node.put("passwordConfirmation", password);
		input.set("input", node);

		doReturn(user.getId()).when(jwtParser).getIdFromVerificationToken(token);

		GraphQLResponse response = template.perform("/mutation/reset-password.graphql", input);
		assertTrue(response.isOk());

		User updatedUser = userRepository.getOne(user.getId());
		assertTrue(passwordEncoder.matches(password, updatedUser.getPassword()));

		verify(emailService).sendPasswordUpdatedEmail(this.user.getEmail());
	}

	@Test
	void resetPassword_user_not_found() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String token = "some.verification.token";
		String password = "newpassword";

		node.put("token", Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)));
		node.put("password", password);
		node.put("passwordConfirmation", password);
		input.set("input", node);

		doReturn(12313).when(jwtParser).getIdFromVerificationToken(token);

		GraphQLResponse response = template.perform("/mutation/reset-password.graphql", input);
		assertTrue(response.isOk());
		assertEquals("User does not exist.", response.get("$.errors[0].message"));

		verify(emailService, never()).sendPasswordUpdatedEmail(anyString());
	}

	@Test
	void resetPassword_specified_password_not_matches() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String token = "some.verification.token";
		String password = "newpassword";

		node.put("token", Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)));
		node.put("password", password);
		node.put("passwordConfirmation", password + "1");
		input.set("input", node);

		doReturn(user.getId()).when(jwtParser).getIdFromVerificationToken(token);

		GraphQLResponse response = template.perform("/mutation/reset-password.graphql", input);
		assertTrue(response.isOk());
		assertEquals("Failed reset password", response.get("$.errors[0].message"));
		assertEquals("Passwords do not match.", response.get("$.errors[0].extensions.exception.errors.passwordConfirmation"));

		verify(emailService, never()).sendPasswordUpdatedEmail(anyString());
	}

	@Test
	void register() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String username = "someone";
		String email = "someone@example.com";
		node.put("username", username);
		node.put("email", email);
		node.put("password", "supersecret");

		input.set("input", node);
		GraphQLResponse response = template.perform("/mutation/register.graphql", input);
		assertTrue(response.isOk());

		User registeredUser = response.get("$.data.register.user", User.class);
		assertTrue(registeredUser.getId() != 0);
		assertEquals(username, registeredUser.getUsername());
		assertEquals(email, registeredUser.getEmail());
		assertEquals("user", registeredUser.getRole());
		assertFalse(registeredUser.getIsActive());

		verify(emailService).sendRegistrationConfirmEmail(eq(username), eq(email), startsWith("/user/confirm?key="));
	}

	@Test
	void register_user_exists() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("username", user.getUsername());
		node.put("email", user.getEmail());
		node.put("password", "supersecret");

		input.set("input", node);
		GraphQLResponse response = template.perform("/mutation/register.graphql", input);
		assertTrue(response.isOk());
		assertEquals("User already exists.", response.get("$.errors[0].message"));
		assertEquals("E-mail already exists.", response.get("$.errors[0].extensions.exception.errors.email"));
		assertEquals("Username already exists.", response.get("$.errors[0].extensions.exception.errors.username"));

		verify(emailService, never()).sendRegistrationConfirmEmail(anyString(), anyString(), anyString());
	}
}
