package com.health.mediconnectx.dto;

public class PatientDTO {

    private Long   id;
    private String name;
    private String email;
    private String birthDate;
    private String gender;
    private Long   height;
    private Long   weight;
    private String category;
    private String contactNumber;
    private String bloodGroup;
    private String allergies;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String address;
    private String imageLink;

    // ── Constructors ──────────────────────────────────────────────────

    public PatientDTO() {}

    /** Full constructor used by service layer */
    public PatientDTO(Long id, String name, String email,
                      String birthDate, String gender,
                      Long height, Long weight, String category,
                      String contactNumber, String bloodGroup,
                      String allergies, String emergencyContactName,
                      String emergencyContactNumber, String address,
                      String imageLink) {
        this.id                     = id;
        this.name                   = name;
        this.email                  = email;
        this.birthDate              = birthDate;
        this.gender                 = gender;
        this.height                 = height;
        this.weight                 = weight;
        this.category               = category;
        this.contactNumber          = contactNumber;
        this.bloodGroup             = bloodGroup;
        this.allergies              = allergies;
        this.emergencyContactName   = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
        this.address                = address;
        this.imageLink              = imageLink;
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

    public Long getHeight() { return height; }
    public void setHeight(Long height) { this.height = height; }

    public Long getWeight() { return weight; }
    public void setWeight(Long weight) { this.weight = weight; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getImageLink() { return imageLink; }
    public void setImageLink(String imageLink) { this.imageLink = imageLink; }
}
