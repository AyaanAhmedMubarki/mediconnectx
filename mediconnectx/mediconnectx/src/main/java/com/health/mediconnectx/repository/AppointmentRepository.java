package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Used by PaymentExpiryScheduler / AppointmentCleanupService to cancel abandoned bookings. */
    List<Appointment> findByStatusAndCreatedAtBefore(AppointmentStatus status, LocalDateTime time);

    /**
     * Used by MissedConsultationScheduler to find BOOKED appointments whose
     * appointment time (+ grace window) has already elapsed.
     */
    List<Appointment> findByStatusAndAppointmentTimeBefore(AppointmentStatus status, LocalDateTime cutoff);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);
}
