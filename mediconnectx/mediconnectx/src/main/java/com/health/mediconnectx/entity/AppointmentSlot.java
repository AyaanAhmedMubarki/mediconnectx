package com.health.mediconnectx.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a pre-generated 15-minute appointment slot for a specific doctor on a specific date.
 *
 * Concurrency safety: the @Version field enables Optimistic Locking so that if two patients
 * attempt to book the same slot simultaneously, only one succeeds. The safeBookSlot repository
 * query provides a second layer by using a conditional UPDATE (WHERE status = OPEN).
 */
@Entity
@Table(
    name = "appointment_slots",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_slot_doctor_date_start",
        columnNames = {"doctor_id", "slot_date", "start_time"}
    )
)
public class AppointmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /** Null until a patient books this slot. */
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    /** Optimistic locking version — prevents double-booking under concurrent requests. */
    @Version
    private Long version;

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
