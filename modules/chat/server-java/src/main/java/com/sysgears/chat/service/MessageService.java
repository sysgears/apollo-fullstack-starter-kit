package com.sysgears.chat.service;

import com.sysgears.chat.dto.*;
import com.sysgears.chat.exception.MessageNotFoundException;
import com.sysgears.chat.model.Message;
import com.sysgears.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
    private final MessageRepository repository;

    @Transactional(readOnly = true)
    public Messages findAll(Pageable pageable) {
        Page<Message> messages = repository.findAll(pageable);

        List<MessageEdges> messageEdgesList = new ArrayList<>();


        for (int i = 0; i < messages.getContent().size(); i++) {
            Message message = messages.getContent().get(i);
            messageEdgesList.add(
                    MessageEdges.builder()
                            .cursor((int) (pageable.getOffset() + i))
                            .node(MessagePayload.from(message))
                            .build()
            );
        }

        return Messages.builder()
                .edges(messageEdgesList)
                .pageInfo(
                        MessagePageInfo.builder()
                                .endCursor((int) (pageable.getOffset() + messages.getSize() - 1))
                                .hasNextPage(!messages.isLast())
                                .build()
                )
                .totalCount(messages.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public CompletableFuture<MessagePayload> getById(Integer id) {
        return repository.findMessageById(id).thenApply(message -> {
            if (message == null) throw new MessageNotFoundException(id);

            return MessagePayload.from(message);
        });
    }

    @Transactional(readOnly = true)
    public Message findById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
    }

    public MessagePayload create(Message message) {
        final Message created = repository.save(message);
        return MessagePayload.from(created);
    }

    public MessagePayload update(Message message) {
        final Message created = repository.save(message);
        return MessagePayload.from(created);
    }

    public Message deleteById(Integer id) {
        final Message message = findById(id);
        repository.delete(message);

        return message;
    }
}
