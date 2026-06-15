package com.health.mediconnectx.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long doctorId;
    private Long patientId;

    private LocalDateTime appointmentTime;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String transactionId;

    /** Optional link back to the AppointmentSlot row that was booked via the slot picker. */
    @Column(name = "slot_id")
    private Long slotId;

    @Lob
    @Column(name = "prescription_file", columnDefinition = "LONGBLOB")
    private byte[] prescriptionFile;

    // ── FEAT-01: Refund tracking ──────────────────────────────────────
    private String refundTransactionId;  // ID of the refund transaction if refunded
    private Integer refundAmount;         // Amount refunded in rupees
    private LocalDateTime refundedAt;     // When the refund was processed
    private String cancellationReason;    // Why the appointment was cancelled

    // ── FEAT-02: Offline booking - Symptoms & details ──────────────────
    @Lob
    private String symptoms;              // Patient's symptoms
    @Lob
    private String notes;                 // Additional appointment notes

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public byte[] getPrescriptionFile() { return prescriptionFile; }
    public void setPrescriptionFile(byte[] prescriptionFile) { this.prescriptionFile = prescriptionFile; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    // ── FEAT-01: Refund getters & setters ──────────────────────────
    public String getRefundTransactionId() { return refundTransactionId; }
    public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }

    public Integer getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Integer refundAmount) { this.refundAmount = refundAmount; }

    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    // ── FEAT-02: Symptoms & notes getters & setters ──────────────────
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
