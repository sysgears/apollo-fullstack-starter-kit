package com.sysgears.mailer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "spring.mail", name = "username", havingValue = "EMAIL_USER")
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }
}
