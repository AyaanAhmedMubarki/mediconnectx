package com.health.mediconnectx.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for creating and reading DoctorShift records.
 *
 * shiftDate   — serialized as "yyyy-MM-dd"  (ISO date, matches <input type="date">)
 * startTime   — serialized as "HH:mm"
 * endTime     — serialized as "HH:mm"
 */
public class ShiftDTO {

    private Long id;
    private Long doctorId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate shiftDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    public ShiftDTO() {}

    public ShiftDTO(Long id, Long doctorId, LocalDate shiftDate,
                    LocalTime startTime, LocalTime endTime) {
        this.id        = id;
        this.doctorId  = doctorId;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime   = endTime;
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
