package com.health.mediconnectx.dto;

public class PatientDTO {

    private Long id;
    private String name; // From User entity
    private String birthDate;
    private String category;
    private String gender;
    private Long height;
    private Long weight;
    private String imageLink;
    private String email;
    // default constructor
    public PatientDTO(){

    }

    public PatientDTO(String name, String birthDate, String gender, Long id, Long weight, String imageLink, Long height, String category, String email) {
        this.birthDate = birthDate;
        this.category = category;
        this.gender = gender;
        this.height = height;
        this.id = id;
        this.imageLink = imageLink;
        this.name = name;
        this.weight = weight;
        this.email = email;
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

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
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

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
