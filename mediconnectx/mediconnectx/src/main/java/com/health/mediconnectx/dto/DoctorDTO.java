package com.health.mediconnectx.dto;

public class DoctorDTO {

    private Long     id;
    private String   name;
    private String   email;
    private String   birthDate;
    private String   gender;
    private String   category;
    private String   licenseNumber;
    private String   experience;
    private Integer  consultationFee; // VAL-03 fix: numeric type
    private String   contactNumber;
    private String   bio;
    private String   imageLink;

    // ── Constructors ──────────────────────────────────────────────────

    public DoctorDTO() {}

    /** Full constructor used by service layer */
    public DoctorDTO(Long id, String name, String email,
                     String birthDate, String gender, String category,
                     String licenseNumber, String experience,
                     Integer consultationFee, String contactNumber,
                     String bio, String imageLink) {
        this.id             = id;
        this.name           = name;
        this.email          = email;
        this.birthDate      = birthDate;
        this.gender         = gender;
        this.category       = category;
        this.licenseNumber  = licenseNumber;
        this.experience     = experience;
        this.consultationFee = consultationFee;
        this.contactNumber  = contactNumber;
        this.bio            = bio;
        this.imageLink      = imageLink;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public Integer getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Integer consultationFee) { this.consultationFee = consultationFee; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getImageLink() { return imageLink; }
    public void setImageLink(String imageLink) { this.imageLink = imageLink; }
}
