package com.health.mediconnectx.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.health.mediconnectx.entity.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Outbound DTO returned to the patient when browsing available slots.
 * Date and time fields are serialized as ISO strings (not arrays) via @JsonFormat.
 */
public class SlotDTO {

    private Long id;
    private Long doctorId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate slotDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private SlotStatus status;

    public SlotDTO() {}

    public SlotDTO(Long id, Long doctorId, LocalDate slotDate,
                   LocalTime startTime, LocalTime endTime, SlotStatus status) {
        this.id        = id;
        this.doctorId  = doctorId;
        this.slotDate  = slotDate;
        this.startTime = startTime;
        this.endTime   = endTime;
        this.status    = status;
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
}
