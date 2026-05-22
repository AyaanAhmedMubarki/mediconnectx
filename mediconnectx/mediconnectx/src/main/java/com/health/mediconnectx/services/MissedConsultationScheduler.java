package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background job that automatically marks consultations as MISSED when the
 * doctor failed to join before the appointment window closed.
 *
 * Detection rule:
 *   Status is still BOOKED  AND  appointmentTime + 15 min (one slot duration) < now
 *
 * When marked MISSED:
 *   - The refund is considered automatically processed (mock system).
 *   - Gross Revenue calculations exclude MISSED appointments.
 *   - The patient's badge on their Consultations page reads "Missed – Refund Processed".
 *   - The AppointmentSlot is NOT restored (it belongs to a time already in the past).
 *
 * @EnableScheduling is present on MediconnectxApplication, so no extra config is needed.
 */
@Component
public class MissedConsultationScheduler {

    /** Grace period — one full 15-minute appointment slot duration. */
    private static final long MISSED_AFTER_MINUTES = 15;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Runs every 5 minutes.
     * Finds every BOOKED appointment whose appointmentTime + 15 min is in the past
     * and marks it MISSED (refund implied).
     */
    @Scheduled(fixedDelay = 300_000)   // every 5 minutes
    @Transactional
    public void markMissedConsultations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(MISSED_AFTER_MINUTES);

        List<Appointment> missedList = appointmentRepository
                .findByStatusAndAppointmentTimeBefore(AppointmentStatus.BOOKED, cutoff);

        for (Appointment apt : missedList) {
            apt.setStatus(AppointmentStatus.MISSED);
        }

        if (!missedList.isEmpty()) {
            appointmentRepository.saveAll(missedList);
            System.out.printf("[MissedConsultationScheduler] Marked %d appointment(s) as MISSED at %s%n",
                    missedList.size(), LocalDateTime.now());
        }
    }
}
