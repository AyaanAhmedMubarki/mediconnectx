package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** DATA-03 fix: Used by PaymentExpiryScheduler to cancel abandoned bookings. */
    List<Appointment> findByStatusAndCreatedAtBefore(AppointmentStatus status, LocalDateTime time);

    /**
     * Used by MissedConsultationScheduler to find BOOKED appointments whose
     * appointment time (+ grace window) has already elapsed.
     */
    List<Appointment> findByStatusAndAppointmentTimeBefore(AppointmentStatus status, LocalDateTime cutoff);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    /**
     * VAL-02 fix: Check for existing PENDING_PAYMENT appointments within a 30-minute window.
     * Prevents double-booking when using free-form date picker (no slot ID).
     * Looks for any PENDING_PAYMENT appointment by the same patient with the same doctor
     * within ±30 minutes of the requested appointment time.
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.patientId = :patientId AND a.doctorId = :doctorId AND " +
           "a.status = com.health.mediconnectx.entity.AppointmentStatus.PENDING_PAYMENT AND " +
           "a.appointmentTime >= :startTime AND a.appointmentTime <= :endTime")
    Optional<Appointment> findExistingPendingPaymentInTimeWindow(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
