package com.health.mediconnectx.dto;

public class AdminDTO {

    private Long id;
    private String name; // From User entity
    private String email; // From User entity
    private String experience;
    private String profileImage;

    public AdminDTO(Long id, String name, String email, String experience, String profileImage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.experience = experience;
        this.profileImage = profileImage;
    }

    // Default constructor
    public AdminDTO() {
    }

    // Getters and setters


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}

