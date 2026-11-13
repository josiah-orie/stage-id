package com.jovine360.talenthub.notification.application.service;

import com.jovine360.talenthub.notification.domain.model.EmailOutbox;
import com.jovine360.talenthub.notification.domain.repository.EmailOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.time.Instant;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_ATTEMPTS = 3;

    private final EmailOutboxRepository outboxRepository;
    private final JavaMailSender mailSender;

    public EmailService(EmailOutboxRepository outboxRepository, JavaMailSender mailSender) {
        this.outboxRepository = outboxRepository;
        this.mailSender = mailSender;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void sendPending() {
        List<EmailOutbox> pending = outboxRepository.findByStatusAndAttemptsLessThan("PENDING", MAX_ATTEMPTS);
        for (EmailOutbox email : pending) {
            trySend(email);
        }
        // Retry FAILED where attempts < MAX_ATTEMPTS
        List<EmailOutbox> failed = outboxRepository.findByStatusAndAttemptsLessThan("FAILED", MAX_ATTEMPTS);
        for (EmailOutbox email : failed) {
            trySend(email);
        }
    }

    private void trySend(EmailOutbox email) {
        email.setAttempts(email.getAttempts() + 1);
        email.setLastAttemptAt(Instant.now());
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(email.getRecipientEmail());
            helper.setSubject(email.getSubject());
            helper.setText(email.getBodyHtml() != null ? email.getBodyHtml() : "", true);
            mailSender.send(msg);
            email.setStatus("SENT");
            email.setSentAt(Instant.now());
        } catch (MailException | MessagingException e) {
            log.warn("Failed to send email to {}: {}", email.getRecipientEmail(), e.getMessage());
            email.setStatus("FAILED");
            email.setErrorMessage(e.getMessage());
        }
        outboxRepository.save(email);
    }
}
