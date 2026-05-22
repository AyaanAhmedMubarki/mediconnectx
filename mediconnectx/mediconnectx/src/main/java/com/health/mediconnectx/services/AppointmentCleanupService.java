package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Frees up abandoned booking slots.
 * If a patient initiated a booking but never completed payment within 10 minutes,
 * the appointment is automatically cancelled — preventing ghost-blocked slots.
 */
@Service
public class AppointmentCleanupService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300_000)
    public void cleanUpAbandonedPayments() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        List<Appointment> abandoned = appointmentRepository
                .findByStatusAndCreatedAtBefore(AppointmentStatus.PENDING_PAYMENT, tenMinutesAgo);

        for (Appointment apt : abandoned) {
            apt.setStatus(AppointmentStatus.CANCELLED);
        }

        if (!abandoned.isEmpty()) {
            appointmentRepository.saveAll(abandoned);
            System.out.println("[CleanupJob] Cancelled " + abandoned.size()
                    + " abandoned appointment(s) at " + LocalDateTime.now());
        }
    }
}
