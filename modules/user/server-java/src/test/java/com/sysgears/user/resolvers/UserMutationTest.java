package com.sysgears.user.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.authentication.utils.SessionUtils;
import com.sysgears.user.config.JWTPreAuthenticationToken;
import com.sysgears.user.dto.UserPayload;
import com.sysgears.user.model.User;
import com.sysgears.user.model.UserAuth;
import com.sysgears.user.model.UserProfile;
import com.sysgears.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserMutationTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private UserRepository userRepository;


	@Test
	void addUser() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode profile = mapper.createObjectNode();

		node.put("username", "user");
		node.put("password", "supersecret");
		node.put("role", "USER");
		node.put("isActive", true);
		node.put("email", "user@sysgears.com");

		profile.put("firstName", "Edward");
		profile.put("lastName", "Fillmore");
		node.set("profile", profile);

		input.set("input", node);
		GraphQLResponse response = template.perform("mutation/add-user.graphql", input);

		assertTrue(response.isOk());
		UserPayload payload = response.get("$.data.addUser", UserPayload.class);
		User createdUser = payload.getUser();

		assertEquals("user", createdUser.getUsername());
		assertEquals("USER", createdUser.getRole());
		assertTrue(createdUser.getIsActive());
		assertEquals("user@sysgears.com", createdUser.getEmail());
		assertEquals("Edward Fillmore", createdUser.getProfile().getFullName());
		assertNull(createdUser.getAuth());
	}

	@Test
	void editUser() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode profile = mapper.createObjectNode();
		ObjectNode auth = mapper.createObjectNode();

		node.put("id", 1);
		node.put("username", "admin");
		node.put("password", "supersecret");
		node.put("role", "ADMIN");
		node.put("isActive", true);
		node.put("email", "admin@sysgears.com");

		profile.put("firstName", "John");
		profile.put("lastName", "Sinna");
		node.set("profile", profile);

		ObjectNode facebook = mapper.createObjectNode();
		facebook.put("fbId", "fb_id");
		facebook.put("displayName", "some");
		ObjectNode google = mapper.createObjectNode();
		google.put("googleId", "g_id");
		google.put("displayName", "google");
		ObjectNode github = mapper.createObjectNode();
		github.put("ghId", "gh_id");
		github.put("displayName", "github");
		ObjectNode certificate = mapper.createObjectNode();
		certificate.put("serial", "some_unique_id");
		ObjectNode linkedin = mapper.createObjectNode();
		linkedin.put("lnId", "ln_id");
		linkedin.put("displayName", "LinkedIn");
		auth.set("facebook", facebook);
		auth.set("google", google);
		auth.set("github", github);
		auth.set("certificate", certificate);
		auth.set("linkedin", linkedin);
		node.set("auth", auth);

		input.set("input", node);
		GraphQLResponse response = template.perform("mutation/edit-user.graphql", input);

		assertTrue(response.isOk());
		UserPayload payload = response.get("$.data.editUser", UserPayload.class);
		User createdUser = payload.getUser();

		assertEquals(1, createdUser.getId());
		assertEquals("admin", createdUser.getUsername());
		assertEquals("ADMIN", createdUser.getRole());
		assertTrue(createdUser.getIsActive());
		assertEquals("admin@sysgears.com", createdUser.getEmail());
		assertEquals("John Sinna", createdUser.getProfile().getFullName());

		UserAuth userAuth = createdUser.getAuth();
		assertNotNull(userAuth);
		assertEquals("some_unique_id", userAuth.getCertificate().getSerial());
		assertEquals("fb_id", userAuth.getFacebook().getFbId());
		assertEquals("some", userAuth.getFacebook().getDisplayName());
		assertEquals("g_id", userAuth.getGoogle().getGoogleId());
		assertEquals("google", userAuth.getGoogle().getDisplayName());
		assertEquals("gh_id", userAuth.getGithub().getGhId());
		assertEquals("github", userAuth.getGithub().getDisplayName());
		assertEquals("ln_id", userAuth.getLinkedin().getLnId());
		assertEquals("LinkedIn", userAuth.getLinkedin().getDisplayName());
	}

	@Test
	void editUser_with_already_saved_profile_data() throws IOException {
		final User user = userRepository.findById(1).get();
		user.setProfile(new UserProfile("James", "Abrams"));
		userRepository.save(user);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode profile = mapper.createObjectNode();

		node.put("id", 1);
		node.put("username", "admin");
		node.put("role", "ADMIN");
		node.put("isActive", true);
		node.put("email", "admin@sysgears.com");

		profile.put("firstName", "John");
		profile.put("lastName", "Sinna");
		node.set("profile", profile);

		input.set("input", node);
		GraphQLResponse response = template.perform("mutation/edit-user.graphql", input);

		assertTrue(response.isOk());
		UserPayload payload = response.get("$.data.editUser", UserPayload.class);
		User createdUser = payload.getUser();

		assertEquals(1, createdUser.getId());
		assertEquals("admin", createdUser.getUsername());
		assertEquals("ADMIN", createdUser.getRole());
		assertTrue(createdUser.getIsActive());
		assertEquals("admin@sysgears.com", createdUser.getEmail());
		assertEquals("John Sinna", createdUser.getProfile().getFullName());
	}

	@Test
	void deleteUser() throws IOException {
		User admin = userRepository.findByUsernameOrEmail("admin").join();
		SessionUtils.SECURITY_CONTEXT.setAuthentication(new JWTPreAuthenticationToken(admin, null));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", 2);

		GraphQLResponse response = template.perform("mutation/delete-user.graphql", node);

		UserPayload payload = response.get("$.data.deleteUser", UserPayload.class);
		User deletedUser = payload.getUser();

		assertEquals(2, deletedUser.getId());
	}

	@Test
	void deleteUser_not_exists() throws IOException {
		userRepository.deleteById(2);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", 2);

		GraphQLResponse response = template.perform("mutation/delete-user.graphql", node);

		assertEquals("User with id 2 does not exist.", response.get("$.errors[0].message"));
	}

	@Test
	void deleteUser_admin_not_login() throws IOException {
		SessionUtils.SECURITY_CONTEXT.setAuthentication(null);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", 2);

		GraphQLResponse response = template.perform("mutation/delete-user.graphql", node);

		assertEquals("You have not enough permissions to delete users.", response.get("$.errors[0].message"));
	}

	@Test
	void deleteUser_not_enough_permission() throws IOException {
		User user = userRepository.findByUsernameOrEmail("user").join();
		SessionUtils.SECURITY_CONTEXT.setAuthentication(new JWTPreAuthenticationToken(user, null));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", 1);

		GraphQLResponse response = template.perform("mutation/delete-user.graphql", node);

		assertEquals("You have not enough permissions to delete users.", response.get("$.errors[0].message"));
	}

	@Test
	void deleteUser_try_delete_yourself() throws IOException {
		User admin = userRepository.findByUsernameOrEmail("admin").join();
		SessionUtils.SECURITY_CONTEXT.setAuthentication(new JWTPreAuthenticationToken(admin, null));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", 1);

		GraphQLResponse response = template.perform("mutation/delete-user.graphql", node);

		assertEquals("You can not delete your self.", response.get("$.errors[0].message"));
	}
}
