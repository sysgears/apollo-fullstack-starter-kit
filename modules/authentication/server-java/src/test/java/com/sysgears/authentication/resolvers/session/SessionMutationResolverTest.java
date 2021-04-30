package com.sysgears.authentication.resolvers.session;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.authentication.utils.SessionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class SessionMutationResolverTest {
	@Autowired
	private GraphQLTestTemplate template;

	@Test
	void logout() throws IOException {
		SessionUtils.SECURITY_CONTEXT.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password"));
		GraphQLResponse response = template.postForResource("logout.graphql");

		assertTrue(response.isOk());
		assertNull(SessionUtils.SECURITY_CONTEXT.getAuthentication());
		assertNull(response.get("$.data.logout"));
	}
}
