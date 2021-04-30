package com.sysgears.post.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.post.dto.CommentPayload;
import com.sysgears.post.dto.PostEdges;
import com.sysgears.post.dto.PostPayload;
import com.sysgears.post.dto.Posts;
import com.sysgears.post.model.Comment;
import com.sysgears.post.model.Post;
import com.sysgears.post.repository.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class PostQueryResolverTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private PostRepository postRepository;
	private List<Post> postList;
	@BeforeEach
	void init() {
		postList = preparePosts();
	}

	@Test
	void posts_with_default_limit() throws IOException {
		GraphQLResponse response = template.postForResource("post/get-posts.graphql");

		assertTrue(response.isOk());
		Posts posts = response.get("$.data.posts", Posts.class);
		assertEquals(postList.size(), posts.getTotalCount());
		assertEquals(postList.size() - 1, posts.getPageInfo().getEndCursor());
		assertFalse(posts.getPageInfo().getHasNextPage());

		List<PostPayload> expected = postList.stream().map(post -> new PostPayload(post.getId(), post.getTitle(), post.getContent())).collect(Collectors.toList());
		List<PostPayload> actual = posts.getEdges().stream().map(PostEdges::getNode).collect(Collectors.toList());
		Assertions.assertThat(actual).containsExactlyInAnyOrder(expected.toArray(PostPayload[]::new));
	}

	@Test
	void posts_with_custom_limit() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("limit", 5);
		node.put("after", 0);

		GraphQLResponse response = template.perform("post/get-posts.graphql", node);

		assertTrue(response.isOk());
		Posts posts = response.get("$.data.posts", Posts.class);
		assertEquals(postList.size(), posts.getTotalCount());
		assertEquals(4, posts.getPageInfo().getEndCursor());
		assertTrue(posts.getPageInfo().getHasNextPage());

		List<PostPayload> expected = postList.subList(0, 5).stream().map(post -> new PostPayload(post.getId(), post.getTitle(), post.getContent())).collect(Collectors.toList());
		List<PostPayload> actual = posts.getEdges().stream().map(PostEdges::getNode).collect(Collectors.toList());

		Assertions.assertThat(actual).containsExactlyInAnyOrder(expected.toArray(PostPayload[]::new));
	}

	@Test
	void post() throws IOException {
		Post expectedPost = postList.get(0);
		List<CommentPayload> expectedComments = expectedPost.getComments().stream().map(comment -> new CommentPayload(comment.getId(), comment.getContent())).collect(Collectors.toList());
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("id", expectedPost.getId());

		GraphQLResponse response = template.perform("post/get-post.graphql", node);

		assertTrue(response.isOk());
		PostPayload postPayload = response.get("$.data.post", PostPayload.class);
		assertEquals(expectedPost.getId(), postPayload.getId());
		assertEquals(expectedPost.getTitle(), postPayload.getTitle());
		assertEquals(expectedPost.getContent(), postPayload.getContent());
		Assertions.assertThat(postPayload.getComments()).containsExactlyInAnyOrder(expectedComments.toArray(CommentPayload[]::new));
	}

	@Test
	void post_not_fount() throws IOException {
		int notExistentPostId = 56562777;

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("id", notExistentPostId);

		GraphQLResponse response = template.perform("post/get-post.graphql", node);

		assertTrue(response.isOk());
		assertEquals(String.format("Post with id %d not found", notExistentPostId), response.get("$.errors[0].message"));
	}

	private List<Post> preparePosts() {
		postRepository.deleteAll();

		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Post post = new Post("title " + i, "content " + i);
			post.addComment(new Comment("comment 1 for post " + i));
			post.addComment(new Comment("comment 2 for post " + i));
			posts.add(post);
		}
		return postRepository.saveAll(posts);
	}
}
