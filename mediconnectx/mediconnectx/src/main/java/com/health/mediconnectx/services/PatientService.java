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

    public PatientDTO getPatientByEmail(String email) {

        // Find the user by email
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }


        // Find the associated Athlete profile by userId
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        // Return PatientDTO with user and patient data
        PatientDTO patientDTO = new PatientDTO(
                user.getName(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getId(),
                patient.getWeight(),
                patient.getImageLink(),
                patient.getHeight(),
                patient.getCategory(),
                user.getEmail()
        );

        return patientDTO;
    }

    public void updatePatientByEmail(String email, PatientDTO patientDTOObject, byte[] imageBytes) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setName(patientDTOObject.getName());

        // Find the associated Admin profile by userId
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Athlete profile not found"));
        patient.setBirthDate(patientDTOObject.getBirthDate());
        patient.setGender(patientDTOObject.getGender());
        patient.setWeight(patientDTOObject.getWeight());
        patient.setHeight(patientDTOObject.getHeight());
        patient.setCategory(patientDTOObject.getCategory());

        // Convert byte[] to Base64 string and set it in the Patient entity
        if (imageBytes != null && imageBytes.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            patient.setImageLink(base64Image);
        }

        userRepository.save(user);
        patientRepository.save(patient);
    }

    public PatientDTO getPatientById(Long id) {

        // Find the associated Patient profile by id
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        User user = patient.getUser();

        // Return PatientDTO with user and patient data
        PatientDTO patientDTO = new PatientDTO(
                user.getName(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getId(),
                patient.getWeight(),
                patient.getImageLink(),
                patient.getHeight(),
                patient.getCategory(),
                user.getEmail()
        );

        return patientDTO;
    }

    public List<PatientDTO> getAllPatient() {
        List<Patient> patients = patientRepository.findAll();

        // Transform entities into DTOs
        return patients.stream()
                .map(patient -> new PatientDTO(
                        patient.getUser().getName(),
                        patient.getBirthDate(),
                        patient.getGender(),
                        patient.getId(),
                        patient.getWeight(),
                        patient.getImageLink(),
                        patient.getHeight(),
                        patient.getCategory(),
                        patient.getUser().getEmail()
                ))
                .collect(Collectors.toList());
    }
}
