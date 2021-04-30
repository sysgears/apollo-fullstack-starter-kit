package com.sysgears.post.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.post.dto.PostPayload;
import com.sysgears.post.model.Post;
import com.sysgears.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class PostMutationResolverTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private PostRepository postRepository;

	@Test
	void addPost() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String title = "Post title";
		String content = "Post content";
		node.put("title", title);
		node.put("content", content);

		input.set("input", node);
		GraphQLResponse response = template.perform("post/add-post.graphql", input);

		assertTrue(response.isOk());
		PostPayload postPayload = response.get("$.data.addPost", PostPayload.class);
		assertNotNull(postPayload.getId());
		assertEquals(title, postPayload.getTitle());
		assertEquals(content, postPayload.getContent());
		assertTrue(postPayload.getComments().isEmpty());
	}

	@Test
	void deletePost() throws IOException {
		Post post = postRepository.saveAndFlush(new Post("Title", "Content"));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", post.getId());

		GraphQLResponse response = template.perform("post/delete-post.graphql", node);

		assertTrue(response.isOk());
		PostPayload postPayload = response.get("$.data.deletePost", PostPayload.class);
		assertEquals(post.getId(), postPayload.getId());
		assertEquals(post.getTitle(), postPayload.getTitle());
		assertEquals(post.getContent(), postPayload.getContent());
	}

	@Test
	void deletePost_with_invalid_id() throws IOException {
		int invalidPostId = 23456;
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("id", invalidPostId);

		GraphQLResponse response = template.perform("post/delete-post.graphql", node);

		assertTrue(response.isOk());
		assertEquals(String.format("Post with id %d not found", invalidPostId), response.get("$.errors[0].message"));
	}

	@Test
	void editPost() throws IOException {
		Post post = postRepository.saveAndFlush(new Post("Title", "Content"));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String title = "Post title";
		String content = "Post content";
		node.put("id", post.getId());
		node.put("title", title);
		node.put("content", content);

		input.set("input", node);
		GraphQLResponse response = template.perform("post/edit-post.graphql", input);

		assertTrue(response.isOk());
		PostPayload postPayload = response.get("$.data.editPost", PostPayload.class);
		assertEquals(post.getId(), postPayload.getId());
		assertEquals(title, postPayload.getTitle());
		assertEquals(content, postPayload.getContent());
		assertTrue(postPayload.getComments().isEmpty());
	}

	@Test
	void editPost_with_invalid_id() throws IOException {
		int invalidPostId = 99999;
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("id", invalidPostId);
		node.put("title", "New title");
		node.put("content", "New content");

		input.set("input", node);
		GraphQLResponse response = template.perform("post/edit-post.graphql", input);

		assertTrue(response.isOk());
		assertEquals(String.format("Post with id %d not found", invalidPostId), response.get("$.errors[0].message"));
	}
}
