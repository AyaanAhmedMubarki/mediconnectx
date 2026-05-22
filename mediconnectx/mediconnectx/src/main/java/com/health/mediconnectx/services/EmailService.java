package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.ContactMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * Sends outbound emails via the configured Gmail SMTP account.
 * All contact-form submissions are forwarded to the support inbox.
 */
@Service
public class EmailService {

    private static final Logger log = Logger.getLogger(EmailService.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String supportEmail;   // support.mediconnectx@gmail.com

    /**
     * Forwards a contact-form submission to the support inbox.
     * The Reply-To header is set to the sender's email so the team can
     * reply directly to the user from their email client.
     * Runs asynchronously so the HTTP response is never blocked by SMTP.
     */
    @Async
    public void sendContactEmail(ContactMessage msg) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();

            email.setFrom(supportEmail);
            email.setTo(supportEmail);
            email.setReplyTo(msg.getEmail());
            email.setSubject("[MediConnectX Contact] " + msg.getSubject() + " – from " + msg.getName());
            email.setText(
                "New message received via the MediConnectX Contact Form\n" +
                "═══════════════════════════════════════════════════════\n\n" +
                "Name    : " + msg.getName()    + "\n" +
                "Email   : " + msg.getEmail()   + "\n" +
                "Subject : " + msg.getSubject() + "\n\n" +
                "Message :\n" + msg.getMessage() + "\n\n" +
                "───────────────────────────────────────────────────────\n" +
                "Submitted at : " + msg.getCreatedAt() + "\n" +
                "MediConnectX – Smart Medical Management System"
            );

            mailSender.send(email);
            log.info("[EmailService] Contact email sent successfully to " + supportEmail);

        } catch (Exception ex) {
            log.severe("[EmailService] Failed to send contact email: " + ex.getMessage());
        }
    }
}
