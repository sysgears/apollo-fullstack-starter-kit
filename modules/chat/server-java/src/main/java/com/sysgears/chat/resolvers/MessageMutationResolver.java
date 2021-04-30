package com.sysgears.chat.resolvers;

import com.sysgears.chat.dto.MessagePayload;
import com.sysgears.chat.dto.input.AddMessageInput;
import com.sysgears.chat.dto.input.EditMessageInput;
import com.sysgears.chat.model.Message;
import com.sysgears.chat.service.MessageService;
import com.sysgears.chat.subscription.MessageUpdatedEvent;
import com.sysgears.chat.subscription.Mutation;
import com.sysgears.core.subscription.Publisher;
import com.sysgears.upload.model.FileMetadata;
import com.sysgears.upload.service.FileService;
import com.sysgears.user.model.User;
import com.sysgears.user.service.UserService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Part;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class MessageMutationResolver implements GraphQLMutationResolver {
	private final MessageService messageService;
	private final FileService fileService;
	private final Publisher<MessageUpdatedEvent> messagePublisher;
	private final UserService userService;


	public CompletableFuture<MessagePayload> addMessage(AddMessageInput input, DataFetchingEnvironment environment) {
		return CompletableFuture.supplyAsync(() -> {
			Message message = new Message();
			if (input.getUserId() != null) {
				User user = userService.findUserById(input.getUserId()).join();
				message.setUser(user);
			} else {
				userService.getCurrentAuditor().ifPresent(message::setUser);
			}
			if (input.getQuotedId() != null) {
				Message quotedMessage = messageService.findById(input.getQuotedId());
				message.setQuoted(quotedMessage);
			}

			message.setText(input.getText());
			message.setUuid(UUID.fromString(input.getUuid()));

			resolveAttachment(environment).ifPresent(attachment -> {
				FileMetadata fileMetadata = fileService.create(attachment);
				message.setAttachment(fileMetadata);
			});

			MessagePayload created = messageService.create(message);

			messagePublisher.publish(new MessageUpdatedEvent(Mutation.CREATED, created));

			return created;
		});
	}

	public CompletableFuture<MessagePayload> deleteMessage(Integer id) {
		return CompletableFuture.supplyAsync(() -> {
			final Message deleted = messageService.deleteById(id);
			if (deleted.getAttachment() != null) {
				fileService.deleteById(deleted.getAttachment().getId());
			}

			final MessagePayload messagePayload = MessagePayload.from(deleted);
			messagePublisher.publish(new MessageUpdatedEvent(Mutation.DELETED, messagePayload));

			return messagePayload;
		});
	}

	public CompletableFuture<MessagePayload> editMessage(EditMessageInput input) {
		return CompletableFuture.supplyAsync(() -> {
			final Message message = messageService.findById(input.getId());
			message.setText(input.getText());
			if (input.getUserId() != null) {
				User user = userService.findUserById(input.getUserId()).join();
				message.setUser(user);
			} else {
				userService.getCurrentAuditor().ifPresent(message::setUser);
			}
			final MessagePayload updated = messageService.update(message);

			messagePublisher.publish(new MessageUpdatedEvent(Mutation.UPDATED, updated));

			return updated;
		});
	}

	private Optional<Part> resolveAttachment(DataFetchingEnvironment environment) {
		final LinkedHashMap<String, Object> inputMap = environment.getArgument("input");
		return Optional.ofNullable((Part) inputMap.get("attachment"));
	}
}
