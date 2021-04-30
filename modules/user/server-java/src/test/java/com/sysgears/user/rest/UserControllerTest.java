package com.sysgears.user.rest;

import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.authentication.service.jwt.JwtGenerator;
import com.sysgears.user.model.User;
import com.sysgears.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtGenerator jwtGenerator;
	@MockBean
	private UserService userService;
	@Value("${app.redirect.confirm-registration}")
	private String confirmRegistrationRedirectUrl;
	@Value("${app.redirect.reset-password}")
	private String resetPasswordRedirectUrl;

	@Test
	void confirmRegistration() throws Exception {
		User registeredUser = new User("username", "password", "user", false, "someone@example.com");
		registeredUser.setId(33);
		String token = jwtGenerator.generateVerificationToken(JwtUserIdentity.builder().id(registeredUser.getId()).build());

		when(userService.findUserById(registeredUser.getId())).thenReturn(CompletableFuture.completedFuture(registeredUser));

		mockMvc.perform(get("/user/confirm").param("key", token))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(confirmRegistrationRedirectUrl));

		ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
		verify(userService).save(userArgumentCaptor.capture());
		assertTrue(userArgumentCaptor.getValue().getIsActive());
	}

	@Test
	void confirmRegistration_no_user_found() throws Exception {
		int invalidUserId = 543;
		String token = jwtGenerator.generateVerificationToken(JwtUserIdentity.builder().id(invalidUserId).build());

		when(userService.findUserById(invalidUserId)).thenReturn(CompletableFuture.completedFuture(null));

		mockMvc.perform(get("/user/confirm").param("key", token))
				.andDo(print())
				.andExpect(status().isNotFound());

		verify(userService, never()).save(any(User.class));
	}

	@Test
	void initiateResetPassword() throws Exception {
		User user = new User("username", "password", "user", false, "someone@example.com");
		user.setId(33);
		String token = jwtGenerator.generateVerificationToken(JwtUserIdentity.builder().id(user.getId()).build());
		String expectedRedirectedUrl = resetPasswordRedirectUrl + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));

		when(userService.findUserById(user.getId())).thenReturn(CompletableFuture.completedFuture(user));

		mockMvc.perform(get("/user/reset-password").param("key", token))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(expectedRedirectedUrl));
	}

	@Test
	void initiateResetPassword_no_user_found() throws Exception {
		int invalidUserId = 1234;
		String token = jwtGenerator.generateVerificationToken(JwtUserIdentity.builder().id(invalidUserId).build());

		when(userService.findUserById(invalidUserId)).thenReturn(CompletableFuture.completedFuture(null));

		mockMvc.perform(get("/user/reset-password").param("key", token))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
}
