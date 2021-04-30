package com.sysgears.mailer.service;

public interface EmailService {

    void sendRegistrationConfirmEmail(String name, String email, String confirmationLink);

    void sendResetPasswordEmail(String email, String resetPasswordPath);

    void sendPasswordUpdatedEmail(String email);

    void sendContactUsEmail(String name, String email, String content);
}
