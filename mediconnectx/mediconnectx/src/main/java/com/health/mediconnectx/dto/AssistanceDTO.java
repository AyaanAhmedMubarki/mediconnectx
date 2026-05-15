package com.health.mediconnectx.dto;

public class AssistanceDTO {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private String requestDate; // Stored as String
    private String status;
    private String remarks;

    public AssistanceDTO(){
        // general constructor
    }

    public AssistanceDTO(Long patientId, Long doctorId, Long id, String remarks, String requestDate, String status) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.id = id;
        this.remarks = remarks;
        this.requestDate = requestDate;
        this.status = status;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

