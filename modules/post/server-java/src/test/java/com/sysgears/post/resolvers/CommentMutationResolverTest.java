package com.sysgears.post.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.post.dto.CommentPayload;
import com.sysgears.post.model.Comment;
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
class CommentMutationResolverTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private PostRepository postRepository;

	@Test
	void addComment() throws IOException {
		Post post = postRepository.saveAndFlush(new Post("Title", "Content"));

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String content = "Comment content";
		node.put("postId", post.getId());
		node.put("content", content);

		input.set("input", node);
		GraphQLResponse response = template.perform("comment/add-comment.graphql", input);

		assertTrue(response.isOk());
		CommentPayload commentPayload = response.get("$.data.addComment", CommentPayload.class);
		assertNotNull(commentPayload.getId());
		assertEquals(content, commentPayload.getContent());
	}

	@Test
	void addComment_for_non_existent_post() throws IOException {
		int invalidPostId = 111111;

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String content = "Comment content";
		node.put("postId", invalidPostId);
		node.put("content", content);

		input.set("input", node);
		GraphQLResponse response = template.perform("comment/add-comment.graphql", input);

		assertTrue(response.isOk());
		assertEquals(String.format("Post with id %d not found", invalidPostId), response.get("$.errors[0].message"));
	}

	@Test
	void deleteComment() throws IOException {
		Post post = new Post("Title", "Content");
		Comment comment = new Comment("comment...");
		post.addComment(comment);
		Post savedPost = postRepository.saveAndFlush(post);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("id", comment.getId());
		node.put("postId", savedPost.getId());

		input.set("input", node);
		GraphQLResponse response = template.perform("comment/delete-comment.graphql", input);

		assertTrue(response.isOk());
		CommentPayload commentPayload = response.get("$.data.deleteComment", CommentPayload.class);
		assertEquals(comment.getId(), commentPayload.getId());
		assertEquals(comment.getContent(), commentPayload.getContent());
	}

	@Test
	void deleteComment_with_invalid_id() throws IOException {
		Post post = postRepository.saveAndFlush(new Post("Title", "Content"));
		int invalidCommentId = 4123111;
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		node.put("id", invalidCommentId);
		node.put("postId", post.getId());

		input.set("input", node);
		GraphQLResponse response = template.perform("comment/delete-comment.graphql", input);

		assertTrue(response.isOk());
		assertEquals(String.format("Comment with id %d not found", invalidCommentId), response.get("$.errors[0].message"));
	}

	@Test
	void editComment() throws IOException {
		Post post = new Post("Post title", "some post content...");
		Comment comment = new Comment("comment...");
		post.addComment(comment);
		Post savedPost = postRepository.saveAndFlush(post);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String content = "new comment content";
		node.put("id", comment.getId());
		node.put("postId", savedPost.getId());
		node.put("content", content);

		input.set("input", node);
		GraphQLResponse response = template.perform("comment/edit-comment.graphql", input);

		assertTrue(response.isOk());
		CommentPayload commentPayload = response.get("$.data.editComment", CommentPayload.class);
		assertEquals(comment.getId(), commentPayload.getId());
		assertEquals(content, commentPayload.getContent());
	}

	@Test
	void editComment_with_invalid_id() throws IOException {
		Post post = postRepository.saveAndFlush(new Post("Post Title", "Post Content"));
		int invalidCommentId = 662525;
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode input = mapper.createObjectNode();
		ObjectNode node = mapper.createObjectNode();

		String content = "Comment content";
		node.put("id", invalidCommentId);
		node.put("postId", post.getId());
		node.put("content", content);

		input.set("input", node);
		GraphQLResponse response = template.perform("comment/edit-comment.graphql", input);

		assertTrue(response.isOk());
		assertEquals(String.format("Comment with id %d not found", invalidCommentId), response.get("$.errors[0].message"));
	}
}
