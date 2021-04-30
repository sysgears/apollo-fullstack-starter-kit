package com.sysgears.chat.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.chat.dto.MessageEdges;
import com.sysgears.chat.dto.MessagePayload;
import com.sysgears.chat.dto.Messages;
import com.sysgears.chat.model.Message;
import com.sysgears.chat.repository.MessageRepository;
import com.sysgears.chat.service.MessageService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class MessageQueryResolverTest {
	@Autowired
	private GraphQLTestTemplate template;
	@Autowired
	private MessageRepository messageRepository;

	private List<Message> messageList;
	@BeforeEach
	void init() {
		messageRepository.deleteAll();
		messageList = prepare();
	}

	@Test
	void messages_with_default_limit() throws IOException {
		GraphQLResponse response = template.postForResource("get-messages.graphql");

		assertTrue(response.isOk());
		Messages messages = response.get("$.data.messages", Messages.class);
		assertEquals(messageList.size(), messages.getTotalCount());
		assertEquals(49, messages.getPageInfo().getEndCursor());
		assertFalse(messages.getPageInfo().getHasNextPage());

		List<MessagePayload> expected = messageList.stream().map(MessagePayload::from).collect(Collectors.toList());
		List<MessagePayload> actual = messages.getEdges().stream().map(MessageEdges::getNode).collect(Collectors.toList());
		Assertions.assertThat(actual).containsExactlyInAnyOrder(expected.toArray(MessagePayload[]::new));
	}

	@Test
	void messages_with_custom_limit() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("limit", 5);
		node.put("after", 0);

		GraphQLResponse response = template.perform("get-messages.graphql", node);

		assertTrue(response.isOk());
		Messages messages = response.get("$.data.messages", Messages.class);
		assertEquals(messageList.size(), messages.getTotalCount());
		assertEquals(4, messages.getPageInfo().getEndCursor());
		assertTrue(messages.getPageInfo().getHasNextPage());

		List<MessagePayload> expected = messageList.subList(0, 5).stream().map(MessagePayload::from).collect(Collectors.toList());
		List<MessagePayload> actual = messages.getEdges().stream().map(MessageEdges::getNode).collect(Collectors.toList());
		Assertions.assertThat(actual).containsExactlyInAnyOrder(expected.toArray(MessagePayload[]::new));
	}

	@Test
	void message() throws IOException {
		Message expectedMessage = messageList.get(0);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("id", expectedMessage.getId());

		GraphQLResponse response = template.perform("get-message.graphql", node);

		assertTrue(response.isOk());
		MessagePayload messagePayload = response.get("$.data.message", MessagePayload.class);
		assertEquals(expectedMessage.getId(), messagePayload.getId());
		assertEquals(expectedMessage.getText(), messagePayload.getText());
		assertEquals(expectedMessage.getUuid().toString(), messagePayload.getUuid());
	}

	@Test
	void message_not_exists() throws IOException {
		Message expectedMessage = messageList.get(0);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("id", expectedMessage.getId());

		GraphQLResponse response = template.perform("get-message.graphql", node);

		assertTrue(response.isOk());
		MessagePayload messagePayload = response.get("$.data.message", MessagePayload.class);
		assertEquals(expectedMessage.getId(), messagePayload.getId());
		assertEquals(expectedMessage.getText(), messagePayload.getText());
		assertEquals(expectedMessage.getUuid().toString(), messagePayload.getUuid());
	}

	private List<Message> prepare() {
		List<Message> messages = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Message message  = new Message();
			message.setText("message " + i);
			message.setUuid(UUID.randomUUID());
			messages.add(message);
		}
		return messageRepository.saveAll(messages);
	}
}
