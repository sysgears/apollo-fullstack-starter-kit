package com.sysgears.mailer.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Optional;

@Service
public class DefaultEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String baseUrl;
    private final String profileRedirectedUrl;
    private final String emailSender;


    public DefaultEmailService(JavaMailSender mailSender,
                               SpringTemplateEngine templateEngine,
                               @Value("${app.server.baseUrl}") String baseUrl,
                               @Value("${app.redirect.profile}") String profileRedirectedUrl,
                               @Value("${spring.mail.username}") String emailSender) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.baseUrl = baseUrl;
        this.profileRedirectedUrl = profileRedirectedUrl;
        this.emailSender = emailSender;
    }

    public void sendRegistrationConfirmEmail(String name, String email, String confirmationPath) {
        Context context = new Context();
        String confirmationLink = UriComponentsBuilder.newInstance()
                .scheme(baseUrl.contains("localhost") ? "http" : "https")
                .host(baseUrl)
                .path(confirmationPath)
                .build()
                .toString();
        context.setVariables(Map.of("name", name, "followLink", confirmationLink));

        String emailBody = templateEngine.process("confirm-registration", context);

        mailSender.send(createMessage(Optional.empty(), email, "Confirm Email", emailBody));
    }

    public void sendResetPasswordEmail(String email, String resetPasswordPath) {
        Context context = new Context();
        String confirmationLink = UriComponentsBuilder.newInstance()
                .scheme(baseUrl.contains("localhost") ? "http" : "https")
                .host(baseUrl)
                .path(resetPasswordPath)
                .build()
                .toString();
        context.setVariables(Map.of("followLink", confirmationLink));

        String emailBody = templateEngine.process("reset-password", context);

        mailSender.send(createMessage(Optional.empty(), email, "Reset Password", emailBody));
    }

    public void sendPasswordUpdatedEmail(String email) {
        Context context = new Context();
        context.setVariables(Map.of("followLink", profileRedirectedUrl));

        String emailBody = templateEngine.process("password-updated", context);

        mailSender.send(createMessage(Optional.empty(), email, "Your Password Has Been Updated", emailBody));
    }

    @SneakyThrows
    public void sendContactUsEmail(String name, String email, String content) {
        Context context = new Context();
        context.setVariables(Map.of("name", name, "content", content));

        String emailBody = templateEngine.process("contact-us", context);

        mailSender.send(createMessage(Optional.of(email), emailSender, "New email through contact us page", emailBody));
    }

    @SneakyThrows
    private MimeMessage createMessage(Optional<String> from, String to, String subject, String text) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, false, "UTF-8");

        // it will be ignored for gmail
        // https://stackoverflow.com/a/27420030
        if (from.isPresent()) {
            messageHelper.setFrom(from.get());
        } else {
            messageHelper.setFrom(emailSender);
        }

        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(text, true);
        return message;
    }
}
