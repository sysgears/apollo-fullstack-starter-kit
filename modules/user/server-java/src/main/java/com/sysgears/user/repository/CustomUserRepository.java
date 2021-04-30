package com.sysgears.user.repository;

import com.sysgears.user.dto.input.FilterUserInput;
import com.sysgears.user.dto.input.OrderByUserInput;
import com.sysgears.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CustomUserRepository {

    CompletableFuture<List<User>> findByCriteria(Optional<OrderByUserInput> orderBy, Optional<FilterUserInput> filter);

    CompletableFuture<User> findByUsernameOrEmail(String usernameOrEmail);
}
