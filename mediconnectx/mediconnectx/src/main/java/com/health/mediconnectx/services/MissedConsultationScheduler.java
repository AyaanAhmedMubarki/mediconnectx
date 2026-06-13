package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.entity.Doctor;
import com.health.mediconnectx.repository.AppointmentRepository;
import com.health.mediconnectx.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Background job that automatically marks consultations as MISSED when the
 * doctor failed to join before the appointment window closed.
 *
 * Detection rule:
 *   Status is still BOOKED  AND  appointmentTime + 15 min (one slot duration) < now
 *
 * When marked MISSED:
 *   - Full refund (100% of doctor's consultation fee) is automatically processed
 *   - Gross Revenue calculations exclude MISSED appointments
 *   - The patient gets a full refund for the missed appointment
 *   - The AppointmentSlot is NOT restored (it belongs to a time already in the past)
 *   - Admin dashboard shows FULL refund amount for MISSED appointments
 *
 * @EnableScheduling is present on MediconnectxApplication, so no extra config is needed.
 */
@Component
public class MissedConsultationScheduler {

    /** Grace period — one full 15-minute appointment slot duration. */
    private static final long MISSED_AFTER_MINUTES = 15;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Runs every 5 minutes.
     * Finds every BOOKED appointment whose appointmentTime + 15 min is in the past
     * and marks it MISSED with full refund (100% of doctor consultation fee).
     */
    @Scheduled(fixedDelay = 300_000)   // every 5 minutes
    @Transactional
    public void markMissedConsultations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(MISSED_AFTER_MINUTES);

        List<Appointment> missedList = appointmentRepository
                .findByStatusAndAppointmentTimeBefore(AppointmentStatus.BOOKED, cutoff);

        for (Appointment apt : missedList) {
            apt.setStatus(AppointmentStatus.MISSED);

            // Process full refund for MISSED appointments
            Integer doctorFee = 500;  // Default
            try {
                Optional<Doctor> docOpt = doctorRepository.findById(apt.getDoctorId());
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

            // FULL REFUND for MISSED (unlike cancellation which is 50%)
            apt.setRefundAmount(doctorFee);  // 100% of consultation fee
            apt.setRefundTransactionId("REFUND-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            apt.setRefundedAt(LocalDateTime.now());
        }

        if (!missedList.isEmpty()) {
            appointmentRepository.saveAll(missedList);
            System.out.printf("[MissedConsultationScheduler] Marked %d appointment(s) as MISSED with full refund(s) at %s%n",
                    missedList.size(), LocalDateTime.now());
        }
    }
}
