package com.sysgears.authentication.resolvers.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.authentication.model.jwt.Tokens;
import com.sysgears.authentication.service.jwt.JwtGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class JwtMutationResolverTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private JwtGenerator jwtGenerator;
	@Autowired
	private JwtUserIdentityService userService;

	@Test
	void refreshTokens() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		JwtUserIdentity jwtUserIdentity = userService.findById(1).get();

		Tokens tokens = jwtGenerator.generateTokens(jwtUserIdentity);
		node.put("refreshToken", tokens.getRefreshToken());


		GraphQLResponse response = template.perform("refresh-tokens.graphql", node);

		assertTrue(response.isOk());
		assertNotNull(response.get("$.data.refreshTokens", Tokens.class));
	}

	@Test
	void refreshTokens_user_not_found() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		JwtUserIdentity jwtUserIdentity = JwtUserIdentity.builder()
				.id(55555)
				.build();

		Tokens tokens = jwtGenerator.generateTokens(jwtUserIdentity);
		node.put("refreshToken", tokens.getRefreshToken());


		GraphQLResponse response = template.perform("refresh-tokens.graphql", node);

		assertTrue(response.isOk());
		assertEquals("Refresh token is invalid.", response.get("$.errors[0].message"));
	}
}
