package com.sysgears.user.repository;

import com.sysgears.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface UserRepository extends JpaRepository<User, Integer>, CustomUserRepository {
    @Async
    CompletableFuture<User> findUserById(int id);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);
}
