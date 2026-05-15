package com.health.mediconnectx.dto;

public class DoctorDTO {

    private Long id;
    private String name; // From User entity
    private String birthDate;
    private String gender;
    private String category;
    private String imageLink;

    // Default constructor
    public DoctorDTO(){

    }

    public DoctorDTO(Long id, String birthDate, String gender, String category, String imageLink, String name) {
        this.birthDate = birthDate;
        this.gender = gender;
        this.category = category;
        this.id = id;
        this.imageLink = imageLink;
        this.name = name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

