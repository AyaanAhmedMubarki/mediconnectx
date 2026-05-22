package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.ContactMessage;
import com.health.mediconnectx.repository.ContactMessageRepository;
import com.health.mediconnectx.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public endpoint for the Contact Us form.
 * Saves the message to the database, then attempts to e-mail it to the
 * support inbox. The email step is non-fatal – a DB failure is also returned
 * gracefully so the user always gets clear feedback.
 */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private EmailService emailService;

    /**
     * POST /api/contact
     * Body: { "name": "...", "email": "...", "subject": "...", "message": "..." }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> submitContact(@RequestBody ContactMessage msg) {

        // ── Basic server-side validation ──────────────────────────────────
        if (isBlank(msg.getName()))    return bad("Name is required.");
        if (isBlank(msg.getEmail()))   return bad("Email is required.");
        if (!msg.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                                       return bad("Please enter a valid email address.");
        if (isBlank(msg.getSubject())) return bad("Subject is required.");
        if (isBlank(msg.getMessage())) return bad("Message is required.");

        // ── Persist to DB ─────────────────────────────────────────────────
        ContactMessage saved;
        try {
            saved = contactMessageRepository.save(msg);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Could not save your message. Please try again."));
        }

        // ── Send email (non-fatal) ────────────────────────────────────────
        try {
            emailService.sendContactEmail(saved);
        } catch (Exception ex) {
            // Message is already in the DB; email failure is logged but not surfaced
            System.err.println("[ContactController] Email send failed: " + ex.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "Your message has been received. We will get back to you within 24 hours."));
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static ResponseEntity<Map<String, String>> bad(String reason) {
        return ResponseEntity.badRequest().body(Map.of("error", reason));
    }
}
