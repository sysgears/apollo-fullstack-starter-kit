package com.sysgears.chat.repository;

import com.sysgears.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    @Async
    CompletableFuture<Message> findMessageById(Integer id);
}
