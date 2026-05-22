package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.entity.SlotStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import com.health.mediconnectx.repository.AppointmentSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    /**
     * Step 1: Patient clicks "Book" — creates a PENDING_PAYMENT record.
     * Body: { doctorId, patientId, appointmentTime (ISO-8601), slotId? (optional) }
     *
     * When slotId is provided (slot-picker flow):
     *   - Atomically marks the slot PENDING_PAYMENT (returns 409 if already taken)
     *   - Stores slotId on the appointment so mock-success can finalise it
     */
    @Transactional
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiateBooking(@RequestBody Map<String, Object> data) {
        try {
            Long doctorId  = Long.parseLong(data.get("doctorId").toString());
            Long patientId = Long.parseLong(data.get("patientId").toString());

            // Optional slotId — present when the patient used the slot-picker
            Long slotId = null;
            if (data.get("slotId") != null) {
                slotId = Long.parseLong(data.get("slotId").toString());
            }

            // Parse appointment time (datetime-local sends "yyyy-MM-ddTHH:mm" or full ISO)
            LocalDateTime appointmentTime;
            if (data.get("appointmentTime") != null) {
                String raw = data.get("appointmentTime").toString();
                try {
                    appointmentTime = LocalDateTime.parse(raw);
                } catch (DateTimeParseException e) {
                    appointmentTime = LocalDateTime.parse(raw,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                }
            } else {
                appointmentTime = LocalDateTime.now().plusHours(1);
            }

            // Reject past appointment times.
            // For slot-based bookings (slotId present) we only reject if the DATE itself is
            // in the past — if the exact minute has ticked by while the patient was on the
            // booking screen that is fine, the slot was pre-validated by the doctor.
            // For manual bookings (no slotId) we keep the strict "no past datetimes" rule.
            boolean isPast = (slotId != null)
                    ? appointmentTime.toLocalDate().isBefore(LocalDate.now())
                    : appointmentTime.isBefore(LocalDateTime.now());

            if (isPast) {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "Cannot book an appointment in the past. Please select a future date and time.");
                return ResponseEntity.badRequest().body(err);
            }

            // If a slot was selected, atomically claim it (OPEN → PENDING_PAYMENT)
            if (slotId != null) {
                int updated = slotRepository.safeBookSlot(
                        slotId, patientId, SlotStatus.PENDING_PAYMENT, SlotStatus.OPEN);
                if (updated == 0) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "This slot is no longer available. Please choose another time.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
                }
            }

            Appointment appointment = new Appointment();
            appointment.setDoctorId(doctorId);
            appointment.setPatientId(patientId);
            appointment.setAppointmentTime(appointmentTime);
            appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
            appointment.setSlotId(slotId);

            Appointment saved = appointmentRepository.save(appointment);

            // expiresAt = createdAt + 5 minutes (the payment window)
            // Sent to the frontend so it can store and restore a persistent countdown.
            LocalDateTime expiresAt = saved.getCreatedAt().plusMinutes(5);

            Map<String, Object> response = new HashMap<>();
            response.put("appointmentId", saved.getId());
            response.put("status",        "PENDING_PAYMENT");
            response.put("expiresAt",     expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to initiate booking: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * Step 2: Patient submits the payment form.
     * Body: { appointmentId }
     */
    @Transactional
    @PostMapping("/mock-success")
    public ResponseEntity<Map<String, String>> processPayment(@RequestBody Map<String, Object> payload) {
        Long appointmentId = Long.parseLong(payload.get("appointmentId").toString());

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElse(null);

        if (appointment == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Appointment not found."));
        }

        // Already cancelled (e.g. by the expiry scheduler)
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return ResponseEntity.status(410).body(Map.of(
                    "error",   "PAYMENT_EXPIRED",
                    "message", "This booking was automatically cancelled because the payment window expired. Please make a new booking."));
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            return ResponseEntity.status(400).body(
                    Map.of("error", "Invalid appointment state: " + appointment.getStatus()));
        }

        // Hard backend check: patient must pay within 5 minutes of booking initiation
        if (appointment.getCreatedAt() != null &&
                appointment.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(410).body(Map.of(
                    "error",   "PAYMENT_EXPIRED",
                    "message", "The 5-minute payment window has expired. Your slot reservation will be released shortly. Please make a new booking."));
        }

        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setTransactionId("MEDI-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        appointmentRepository.save(appointment);

        // If this appointment was booked via the slot-picker, finalise the slot too
        if (appointment.getSlotId() != null) {
            slotRepository.safeBookSlot(
                    appointment.getSlotId(),
                    appointment.getPatientId(),
                    SlotStatus.BOOKED,
                    SlotStatus.PENDING_PAYMENT);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Payment successful. Appointment confirmed.",
                "transactionId", appointment.getTransactionId()
        ));
    }
}
