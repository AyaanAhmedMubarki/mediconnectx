package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.AppointmentSlot;
import com.health.mediconnectx.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    /**
     * Returns all slots for a doctor on a given date, ordered by start time.
     * Used by the patient-facing slot grid.
     */
    List<AppointmentSlot> findByDoctorIdAndSlotDateOrderByStartTimeAsc(Long doctorId, LocalDate date);

    /**
     * Used by the generation engine to skip dates that already have slots,
     * preventing duplicate generation on repeated calls.
     */
    boolean existsByDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);

    /**
     * Atomic conditional UPDATE: claims a slot (OPEN → PENDING_PAYMENT or BOOKED).
     * Returns 1 on success, 0 if the slot was already taken.
     * flushAutomatically=true forces a flush of all pending dirty entities (e.g. an
     * Appointment already modified in the same transaction) before the bulk UPDATE runs,
     * preventing the subsequent cache clear from discarding those unflushed writes.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AppointmentSlot s " +
           "SET s.status = :newStatus, s.patientId = :patientId " +
           "WHERE s.id = :slotId AND s.status = :openStatus")
    int safeBookSlot(@Param("slotId")    Long slotId,
                     @Param("patientId") Long patientId,
                     @Param("newStatus") SlotStatus newStatus,
                     @Param("openStatus") SlotStatus openStatus);

    /**
     * Returns distinct doctor IDs that have at least one OPEN slot on the given date.
     * Used by the patient booking page to show only doctors with real availability.
     */
    @Query("SELECT DISTINCT s.doctorId FROM AppointmentSlot s WHERE s.slotDate = :date AND s.status = com.health.mediconnectx.entity.SlotStatus.OPEN")
    List<Long> findDoctorIdsWithOpenSlotsOnDate(@Param("date") LocalDate date);

    /**
     * Restores a slot to OPEN when a payment expires or is cancelled.
     * Clears patientId atomically. The WHERE on currentStatus guards against
     * overwriting a slot that was already re-booked by another patient.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AppointmentSlot s SET s.status = :newStatus, s.patientId = NULL " +
           "WHERE s.id = :slotId AND s.status = :currentStatus")
    int restoreSlot(@Param("slotId")        Long slotId,
                    @Param("newStatus")     SlotStatus newStatus,
                    @Param("currentStatus") SlotStatus currentStatus);
}
