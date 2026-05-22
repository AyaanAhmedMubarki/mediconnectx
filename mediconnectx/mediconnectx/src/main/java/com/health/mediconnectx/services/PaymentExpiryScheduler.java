package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.entity.SlotStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import com.health.mediconnectx.repository.AppointmentSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background job that enforces the payment timeout lifecycle:
 *
 *  T + 0 min  Patient books → Appointment = PENDING_PAYMENT, slot = PENDING_PAYMENT
 *  T + 5 min  Frontend timer expires → payment form is disabled (UI guard)
 *             Backend hard-check in processPayment also rejects late submissions (HTTP 410)
 *  T + 10 min This scheduler runs → Appointment = CANCELLED, slot = OPEN
 *
 * The 5-minute grace period between frontend expiry and backend cancellation ensures
 * that any in-flight payment request that was submitted just before the 5-minute mark
 * still has time to reach the server and complete successfully.
 *
 * @EnableScheduling is already present on MediconnectxApplication.
 */
@Component
public class PaymentExpiryScheduler {

    /** 5-minute frontend window + 5-minute backend grace = 10-minute total cut-off. */
    private static final long CANCEL_AFTER_MINUTES = 10;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    /**
     * Runs every 60 seconds.
     * Finds every PENDING_PAYMENT appointment whose createdAt is older than
     * CANCEL_AFTER_MINUTES, marks it CANCELLED, and restores the linked slot to OPEN.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cancelExpiredPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(CANCEL_AFTER_MINUTES);

        List<Appointment> expired = appointmentRepository
                .findByStatusAndCreatedAtBefore(AppointmentStatus.PENDING_PAYMENT, cutoff);

        for (Appointment apt : expired) {
            apt.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(apt);

            if (apt.getSlotId() != null) {
                int restored = slotRepository.restoreSlot(
                        apt.getSlotId(),
                        SlotStatus.OPEN,
                        SlotStatus.PENDING_PAYMENT);
                System.out.printf("[PaymentExpiryScheduler] Slot #%d → %s%n",
                        apt.getSlotId(), restored > 0 ? "OPEN (restored)" : "unchanged (already moved)");
            }

            System.out.printf("[PaymentExpiryScheduler] Appointment #%d CANCELLED (created %s)%n",
                    apt.getId(), apt.getCreatedAt());
        }

        if (!expired.isEmpty()) {
            System.out.printf("[PaymentExpiryScheduler] Cancelled %d expired payment(s).%n",
                    expired.size());
        }
    }
}
