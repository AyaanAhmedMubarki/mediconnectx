package com.health.mediconnectx.dto;

public class EventDTO {

    private Long id;
    private String category;
    private String eventDate;
    private String eventName;
    private String imageLink;
    private String meetName;
    private String location;

    public EventDTO(String category, String meetName, String imageLink, Long id, String eventName, String eventDate, String location) {
        this.category = category;
        this.meetName = meetName;
        this.imageLink = imageLink;
        this.id = id;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.location = location;
    }

    // Default constructor
    public EventDTO() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getMeetName() {
        return meetName;
    }

    public void setMeetName(String meetName) {
        this.meetName = meetName;
    }
}

