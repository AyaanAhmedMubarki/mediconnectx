package com.health.mediconnectx.services;

import com.health.mediconnectx.repository.PatientRepository;
import com.health.mediconnectx.repository.UserRepository;
import com.health.mediconnectx.dto.PatientDTO;
import com.health.mediconnectx.entity.Patient;
import com.health.mediconnectx.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    // ── Helper: entity → DTO ──────────────────────────────────────────
    private PatientDTO toDTO(Patient patient, User user) {
        return new PatientDTO(
                patient.getId(),
                user.getName(),
                user.getEmail(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getHeight(),
                patient.getWeight(),
                patient.getCategory(),
                patient.getContactNumber(),
                patient.getBloodGroup(),
                patient.getAllergies(),
                patient.getEmergencyContactName(),
                patient.getEmergencyContactNumber(),
                patient.getAddress(),
                patient.getImageLink()
        );
    }

    // ── GET by email ──────────────────────────────────────────────────
    public PatientDTO getPatientByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found");

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        return toDTO(patient, user);
    }

    // ── GET by id ─────────────────────────────────────────────────────
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
        return toDTO(patient, patient.getUser());
    }

    // ── GET all ───────────────────────────────────────────────────────
    public List<PatientDTO> getAllPatient() {
        return patientRepository.findAll()
                .stream()
                .map(p -> toDTO(p, p.getUser()))
                .collect(Collectors.toList());
    }

    // ── UPDATE by email ───────────────────────────────────────────────
    public void updatePatientByEmail(String email, PatientDTO dto, byte[] imageBytes) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found");

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        patient.setBirthDate(dto.getBirthDate());
        patient.setGender(dto.getGender());
        patient.setHeight(dto.getHeight());
        patient.setWeight(dto.getWeight());
        patient.setCategory(dto.getCategory());
        patient.setContactNumber(dto.getContactNumber());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAllergies(dto.getAllergies());
        patient.setEmergencyContactName(dto.getEmergencyContactName());
        patient.setEmergencyContactNumber(dto.getEmergencyContactNumber());
        patient.setAddress(dto.getAddress());

        if (imageBytes != null && imageBytes.length > 0) {
            patient.setImageLink(Base64.getEncoder().encodeToString(imageBytes));
        }

        userRepository.save(user);
        patientRepository.save(patient);
    }
}
