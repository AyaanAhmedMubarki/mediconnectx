package com.health.mediconnectx.services;

import com.health.mediconnectx.repository.DoctorRepository;
import com.health.mediconnectx.repository.UserRepository;
import com.health.mediconnectx.dto.DoctorDTO;
import com.health.mediconnectx.entity.Doctor;
import com.health.mediconnectx.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    public DoctorDTO getDoctorByEmail(String email) {
        // Find the user by email
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }


        // Find the associated Coach profile by userId
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Athlete profile not found"));

        // Return CoachDTO with user and coach data
        DoctorDTO doctorDTO = new DoctorDTO(
                doctor.getId(),
                doctor.getBirthDate(),
                doctor.getGender(),
                doctor.getCategory(),
                doctor.getImageLink(),
                user.getName()
        );

        return doctorDTO;
    }

    public void updateDoctorByEmail(String email, DoctorDTO doctorDTOObject, byte[] imageBytes) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setName(doctorDTOObject.getName());

        // Find the associated Coach profile by userId
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Coach profile not found"));
        doctor.setBirthDate(doctorDTOObject.getBirthDate());
        doctor.setGender(doctorDTOObject.getGender());
        doctor.setCategory(doctorDTOObject.getCategory());

        // Convert byte[] to Base64 string and set it in the Athlete entity
        if (imageBytes != null && imageBytes.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            doctor.setImageLink(base64Image);
        }

        userRepository.save(user);
        doctorRepository.save(doctor);
    }

    public List<DoctorDTO> getAllDoctor() {

        List<Doctor> doctors = doctorRepository.findAll();

        // Transform entities into DTOs
        return doctors.stream()
                .map(coach -> new DoctorDTO(
                        coach.getId(),
                        coach.getBirthDate(),
                        coach.getGender(),
                        coach.getCategory(),
                        coach.getImageLink(),
                        coach.getUser().getName() // Extract user's name
                ))
                .collect(Collectors.toList());
    }

    public DoctorDTO getDoctorById(Long id) {

        // Find the associated Athlete profile by id
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Athlete profile not found"));

        User user = doctor.getUser();

        // Return AthleteDTO with user and athlete data
        DoctorDTO doctorDTO = new DoctorDTO(
                doctor.getId(),
                doctor.getBirthDate(),
                doctor.getGender(),
                doctor.getCategory(),
                doctor.getImageLink(),
                doctor.getUser().getName()
        );
        return doctorDTO;
    }
}

