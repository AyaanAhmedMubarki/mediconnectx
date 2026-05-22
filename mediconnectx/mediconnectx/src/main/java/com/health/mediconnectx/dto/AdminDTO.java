package com.health.mediconnectx.dto;

public class AdminDTO {

    private Long id;
    private String name;
    private String email;
    private String profileImage;
    private String contactNumber;

    public AdminDTO(Long id, String name, String email, String profileImage, String contactNumber) {
        this.id            = id;
        this.name          = name;
        this.email         = email;
        this.profileImage  = profileImage;
        this.contactNumber = contactNumber;
    }

    public AdminDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}
