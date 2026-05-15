package com.health.mediconnectx.dto;

public class ResultDTO {

    private Long id;
    private Long patientId;
    private Long eventId;
    private String publishDate; // Stored as String
    private String score;
    private String remarks;


    public ResultDTO(){}

    public ResultDTO(Long patientId, Long eventId, Long id, String publishDate, String score, String remarks) {
        this.patientId = patientId;
        this.eventId = eventId;
        this.id = id;
        this.publishDate = publishDate;
        this.score = score;
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

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
