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
     * Same as above but also requires the slot to start AFTER a given time.
     * Used when the requested date is today so that doctors whose last open slot
     * has already passed are not shown to patients.
     */
    @Query("SELECT DISTINCT s.doctorId FROM AppointmentSlot s WHERE s.slotDate = :date AND s.status = com.health.mediconnectx.entity.SlotStatus.OPEN AND s.startTime > :afterTime")
    List<Long> findDoctorIdsWithOpenSlotsOnDateAfterTime(
            @Param("date")      LocalDate date,
            @Param("afterTime") java.time.LocalTime afterTime);

    /**
     * Returns true if a slot already exists for this doctor at exactly this start time
     * on this date. Used to give per-shift idempotency in generateSlotsForShift().
     */
    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate,
                                                     java.time.LocalTime startTime);

    /**
     * Returns distinct doctor IDs that have ANY slot (any status) on the given date,
     * starting at or after the given time.
     * Used when the requested date is today: shows doctors who are still working
     * (have non-past slots), even when all remaining slots are already taken/booked.
     */
    @Query("SELECT DISTINCT s.doctorId FROM AppointmentSlot s WHERE s.slotDate = :date AND s.startTime >= :fromTime")
    List<Long> findDoctorIdsWithAnySlotsOnDateFromTime(
            @Param("date")     LocalDate date,
            @Param("fromTime") java.time.LocalTime fromTime);

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

    /**
     * Deletes all OPEN (unbooked) slots for a doctor on a specific date
     * that fall within the given start and end times.
     * Used when a doctor removes a shift — booked slots are preserved.
     * Returns the number of slots deleted.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AppointmentSlot s " +
           "WHERE s.doctorId = :doctorId " +
           "AND s.slotDate = :slotDate " +
           "AND s.startTime >= :startTime " +
           "AND s.endTime <= :endTime " +
           "AND s.status = com.health.mediconnectx.entity.SlotStatus.OPEN")
    int deleteOpenSlotsInTimeRange(
            @Param("doctorId") Long doctorId,
            @Param("slotDate") LocalDate slotDate,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);
}
