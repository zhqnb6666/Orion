package org.sustech.orion.service;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendVerificationEmail(String to, String verificationCode);
}
