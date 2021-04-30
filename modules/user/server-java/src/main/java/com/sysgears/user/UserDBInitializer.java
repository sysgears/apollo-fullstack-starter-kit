package com.sysgears.user;

import com.sysgears.user.model.User;
import com.sysgears.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDBInitializer {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent event) {
        long count = repository.count();
        if (count == 0) {
            String encodedAdminPassword = passwordEncoder.encode("admin123");
            String encodedUserPassword = passwordEncoder.encode("user1234");

            User admin = new User("admin", encodedAdminPassword, "admin", true, "admin@example.com");
            User user = new User("user", encodedUserPassword, "user", true, "user@example.com");
            repository.saveAll(List.of(admin, user));
            log.debug("Users initialized");
        }
    }
}
