package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.entity.Doctor;
import com.health.mediconnectx.entity.SlotStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import com.health.mediconnectx.repository.AppointmentSlotRepository;
import com.health.mediconnectx.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * CODE-01 fix: Service layer for appointment operations.
 * Centralizes business logic so the controller doesn't directly access the repository.
 */
@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Retrieve all appointments (admin only).
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * Retrieve a single appointment by ID.
     */
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Retrieve all appointments for a specific patient.
     */
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    /**
     * Retrieve all appointments for a specific doctor.
     */
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    /**
     * Get appointments with a specific status that were created before a cutoff time.
     * Used by schedulers to find expired bookings.
     */
    public List<Appointment> getAppointmentsByStatusAndCreatedBefore(
            AppointmentStatus status, java.time.LocalDateTime cutoff) {
        return appointmentRepository.findByStatusAndCreatedAtBefore(status, cutoff);
    }

    /**
     * Get appointments with a specific status whose appointment time has passed.
     * Used by schedulers to find missed consultations.
     */
    public List<Appointment> getAppointmentsByStatusAndTimeBefore(
            AppointmentStatus status, java.time.LocalDateTime cutoff) {
        return appointmentRepository.findByStatusAndAppointmentTimeBefore(status, cutoff);
    }

    /**
     * FEAT-01: Cancel an appointment and process refund if applicable.
     * Always releases the appointment slot back to OPEN status when cancelled.
     *
     * Refund and Slot Release Logic:
     * - PENDING_PAYMENT: No refund (payment not confirmed), slot released to OPEN
     * - BOOKED (Doctor cancels): Full refund (100% of doctor's consultation fee), slot released to OPEN
     * - BOOKED (Patient cancels): Partial refund (50% of doctor's consultation fee), slot released to OPEN
     * - Other statuses (COMPLETED, MISSED, CANCELLED): Cannot cancel
     *
     * @param appointmentId The appointment ID to cancel
     * @param cancellationReason The reason for cancellation
     * @param isDoctorCancelling Whether the doctor (true) or patient (false) initiated the cancellation
     * @return response map with status, message, refund details, and slotReleased flag
     */
    @Transactional
    public Map<String, Object> cancelAppointment(Long appointmentId, String cancellationReason, boolean isDoctorCancelling) {
        Optional<Appointment> aptOpt = appointmentRepository.findById(appointmentId);
        if (aptOpt.isEmpty()) {
            return Map.of("error", "Appointment not found.", "status", 404);
        }

        Appointment appointment = aptOpt.get();

        // Cannot cancel if already cancelled, completed, or missed
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return Map.of("error", "Appointment is already cancelled.", "status", 400);
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return Map.of("error", "Cannot cancel a completed appointment.", "status", 400);
        }
        if (appointment.getStatus() == AppointmentStatus.MISSED) {
            return Map.of("error", "Cannot cancel a missed appointment.", "status", 400);
        }

        // Save original status BEFORE changing it (needed for refund & slot logic)
        AppointmentStatus originalStatus = appointment.getStatus();

        // Update appointment status to CANCELLED
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(cancellationReason);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("appointmentId", appointmentId);
        response.put("status", "CANCELLED");

        // Handle refund if appointment was BOOKED (payment confirmed)
        if (originalStatus == AppointmentStatus.BOOKED) {
            // Get doctor's actual consultation fee
            Integer doctorFee = 500;  // Default
            try {
                Optional<Doctor> docOpt = doctorRepository.findById(appointment.getDoctorId());
                if (docOpt.isPresent()) {
                    Integer fee = docOpt.get().getConsultationFee();
                    if (fee != null && fee > 0) {
                        doctorFee = fee;
                    }
                }
            } catch (Exception e) {
                // Log and continue with default
                e.printStackTrace();
            }

            // Calculate refund based on who cancelled:
            // - Doctor cancels: Full refund (100%)
            // - Patient cancels: Partial refund (50%)
            Integer refundAmount = isDoctorCancelling ? doctorFee : (doctorFee / 2);
            String refundPercentage = isDoctorCancelling ? "100%" : "50%";

            // Create refund transaction
            String refundTxnId = "REFUND-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            appointment.setRefundTransactionId(refundTxnId);
            appointment.setRefundAmount(refundAmount);
            appointment.setRefundedAt(LocalDateTime.now());

            response.put("refundStatus", "INITIATED");
            response.put("refundTransactionId", refundTxnId);
            response.put("refundAmount", refundAmount);
            response.put("slotReleased", true);
            if (isDoctorCancelling) {
                response.put("message", "Appointment cancelled by doctor. Full refund of ₹" + refundAmount + " (100% of ₹" + doctorFee + ") initiated. Slot released.");
            } else {
                response.put("message", "Appointment cancelled. Refund of ₹" + refundAmount + " (50% of ₹" + doctorFee + ") initiated. Slot released.");
            }
        } else if (originalStatus == AppointmentStatus.PENDING_PAYMENT) {
            response.put("slotReleased", true);
            response.put("message", "Appointment cancelled. No refund (payment was not confirmed). Slot released.");
        }

        // Release the slot if it was booked via slot-picker
        if (appointment.getSlotId() != null) {
            try {
                // Restore slot from its original status (PENDING_PAYMENT or BOOKED) back to OPEN
                SlotStatus slotStatusToRestore = (originalStatus == AppointmentStatus.PENDING_PAYMENT)
                        ? SlotStatus.PENDING_PAYMENT
                        : SlotStatus.BOOKED;
                slotRepository.restoreSlot(appointment.getSlotId(), SlotStatus.OPEN, slotStatusToRestore);
            } catch (Exception e) {
                // Log error but don't fail the cancellation
                e.printStackTrace();
            }
        }

        // Save the updated appointment
        appointmentRepository.save(appointment);

        return response;
    }
}
