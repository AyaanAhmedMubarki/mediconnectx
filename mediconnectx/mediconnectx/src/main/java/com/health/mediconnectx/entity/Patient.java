package com.health.mediconnectx.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String birthDate;
    private String gender;
    private Long   height;
    private Long   weight;
    private String category;

    // ── New health / contact fields ───────────────────────────────────
    private String contactNumber;
    private String bloodGroup;         // e.g. "A+", "O-"
    private String allergies;          // comma-separated, e.g. "Penicillin, Peanuts"
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String address;

    @Lob
    private String imageLink;

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Transient
    public String getName() {
        return user != null ? user.getName() : null;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
