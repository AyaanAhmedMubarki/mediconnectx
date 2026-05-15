package com.health.mediconnectx.dto;

public class RegistrationDTO {

    private Long id;
    private Long patientId;
    private Long eventId;
    private String registrationDate; // Stored as String
    private String status;

    // Constructors
    public RegistrationDTO() {}

    public RegistrationDTO(Long id, Long patientId, Long eventId, String registrationDate, String status) {
        this.id = id;
        this.patientId = patientId;
        this.eventId = eventId;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

