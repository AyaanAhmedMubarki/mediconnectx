package com.health.mediconnectx.dto;

/**
 * Request body for POST /api/v1/slots/{slotId}/book
 */
public class BookingRequestDTO {

    private Long patientId;

    public BookingRequestDTO() {}

    public BookingRequestDTO(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}
