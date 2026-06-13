package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.entity.SlotStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import com.health.mediconnectx.repository.AppointmentSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * CODE-02 fix: Service layer for payment operations.
 * Centralizes payment business logic so the controller stays thin.
 * Abstracts the mock payment implementation for easier future swaps.
 */
@Service
public class PaymentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    /**
     * Validate an appointment time against the current date/time.
     * Returns true if the time is in the past.
     */
    private boolean isAppointmentTimePast(LocalDateTime appointmentTime, Long slotId) {
        if (slotId != null) {
            return appointmentTime.toLocalDate().isBefore(LocalDate.now());
        }
        return appointmentTime.isBefore(LocalDateTime.now());
    }

    /**
     * Parse appointment time from request data.
     * Handles both ISO format and datetime-local format.
     */
    public LocalDateTime parseAppointmentTime(Object timeData) throws DateTimeParseException {
        if (timeData == null) {
            return LocalDateTime.now().plusHours(1);
        }
        String raw = timeData.toString();
        try {
            return LocalDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        }
    }

    /**
     * Initiate a booking (Step 1: Create PENDING_PAYMENT appointment).
     * Validates duplicate bookings and reserves a slot if provided.
     *
     * @return response map with appointmentId, status, and expiresAt
     */
    @Transactional
    public Map<String, Object> initiateBooking(Long patientId, Long doctorId,
                                               Long slotId, LocalDateTime appointmentTime) {
        // Reject past appointment times
        if (isAppointmentTimePast(appointmentTime, slotId)) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Cannot book an appointment in the past. Please select a future date and time.");
            err.put("status", 400);
            return err;
        }

        // VAL-02 fix: Check for duplicate PENDING_PAYMENT bookings within 30-minute window
        LocalDateTime startWindow = appointmentTime.minusMinutes(30);
        LocalDateTime endWindow = appointmentTime.plusMinutes(30);
        if (appointmentRepository.findExistingPendingPaymentInTimeWindow(
                patientId, doctorId, startWindow, endWindow).isPresent()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "You already have a pending booking with this doctor near this time. Please complete or cancel the existing booking first.");
            err.put("status", 409);
            return err;
        }

        // If a slot was selected, atomically claim it (OPEN → PENDING_PAYMENT)
        if (slotId != null) {
            int updated = slotRepository.safeBookSlot(
                    slotId, patientId, SlotStatus.PENDING_PAYMENT, SlotStatus.OPEN);
            if (updated == 0) {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "This slot is no longer available. Please choose another time.");
                err.put("status", 409);
                return err;
            }
        }

        // Create the appointment
        Appointment appointment = new Appointment();
        appointment.setDoctorId(doctorId);
        appointment.setPatientId(patientId);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
        appointment.setSlotId(slotId);

        Appointment saved = appointmentRepository.save(appointment);
        LocalDateTime expiresAt = saved.getCreatedAt().plusMinutes(5);

        Map<String, Object> response = new HashMap<>();
        response.put("appointmentId", saved.getId());
        response.put("status", "PENDING_PAYMENT");
        response.put("expiresAt", expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    /**
     * Process payment (Step 2: Confirm PENDING_PAYMENT → BOOKED).
     * Validates payment window and updates appointment status.
     * Returns success response with transaction ID, or error map.
     */
    @Transactional
    public Map<String, String> processPayment(Long appointmentId) {
        // Fetch appointment
        Optional<Appointment> aptOpt = appointmentRepository.findById(appointmentId);
        if (aptOpt.isEmpty()) {
            return Map.of("error", "Appointment not found.", "status", "404");
        }

        Appointment appointment = aptOpt.get();

        // Check if already cancelled (e.g. by expiry scheduler)
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return Map.of(
                    "error", "PAYMENT_EXPIRED",
                    "message", "This booking was automatically cancelled because the payment window expired. Please make a new booking.",
                    "status", "410");
        }

        // Check if still in PENDING_PAYMENT state
        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            return Map.of("error", "Invalid appointment state: " + appointment.getStatus(),
                    "status", "400");
        }

        // Hard backend check: patient must pay within 5 minutes of booking initiation
        if (appointment.getCreatedAt() != null &&
                appointment.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            return Map.of(
                    "error", "PAYMENT_EXPIRED",
                    "message", "The 5-minute payment window has expired. Your slot reservation will be released shortly. Please make a new booking.",
                    "status", "410");
        }

        // Update appointment to BOOKED
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setTransactionId("MEDI-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        appointmentRepository.save(appointment);

        // If booked via slot-picker, finalize the slot too
        if (appointment.getSlotId() != null) {
            slotRepository.safeBookSlot(
                    appointment.getSlotId(),
                    appointment.getPatientId(),
                    SlotStatus.BOOKED,
                    SlotStatus.PENDING_PAYMENT);
        }

        return Map.of(
                "message", "Payment successful. Appointment confirmed.",
                "transactionId", appointment.getTransactionId());
    }
}
