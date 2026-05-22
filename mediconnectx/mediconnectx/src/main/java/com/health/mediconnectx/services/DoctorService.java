package com.health.mediconnectx.services;

import com.health.mediconnectx.repository.AppointmentSlotRepository;
import com.health.mediconnectx.repository.DoctorRepository;
import com.health.mediconnectx.repository.UserRepository;
import com.health.mediconnectx.dto.DoctorDTO;
import com.health.mediconnectx.entity.Doctor;
import com.health.mediconnectx.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    // ── Helper: entity → DTO ──────────────────────────────────────────
    // Applies two fallbacks:
    //   category null/blank  → "General Practitioner"
    //   consultationFee null/blank → "500"
    private DoctorDTO toDTO(Doctor doctor, User user) {
        String specialization = (doctor.getCategory() == null || doctor.getCategory().isBlank())
                ? "General Practitioner"
                : doctor.getCategory().trim();

        String fee = (doctor.getConsultationFee() == null || doctor.getConsultationFee().isBlank())
                ? "500"
                : doctor.getConsultationFee().trim();

        return new DoctorDTO(
                doctor.getId(),
                user.getName(),
                user.getEmail(),
                doctor.getBirthDate(),
                doctor.getGender(),
                specialization,
                doctor.getLicenseNumber(),
                doctor.getExperience(),
                fee,
                doctor.getContactNumber(),
                doctor.getBio(),
                doctor.getImageLink()
        );
    }

    // ── GET by email ──────────────────────────────────────────────────
    public DoctorDTO getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found");

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        return toDTO(doctor, user);
    }

    // ── GET by id ─────────────────────────────────────────────────────
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        return toDTO(doctor, doctor.getUser());
    }

    // ── GET all ───────────────────────────────────────────────────────
    public List<DoctorDTO> getAllDoctor() {
        return doctorRepository.findAll()
                .stream()
                .map(d -> toDTO(d, d.getUser()))
                .collect(Collectors.toList());
    }

    // ── GET distinct specializations ──────────────────────────────────
    // Returns sorted list of all distinct specializations across all doctors.
    // null / blank category contributes "General Practitioner".
    public List<String> getSpecializations() {
        return doctorRepository.findAll().stream()
                .map(d -> (d.getCategory() == null || d.getCategory().isBlank())
                        ? "General Practitioner"
                        : d.getCategory().trim())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ── GET doctors with optional spec + date filter ───────────────────
    // specialization: if null/blank → no spec filter
    // date:           if non-null   → only doctors with ≥1 OPEN slot on that date
    public List<DoctorDTO> getAvailableDoctors(String specialization, LocalDate date) {
        List<Doctor> doctors = doctorRepository.findAll();

        // Specialisation filter (case-insensitive)
        if (specialization != null && !specialization.isBlank()) {
            String spec = specialization.trim();
            doctors = doctors.stream()
                    .filter(d -> {
                        String cat = (d.getCategory() == null || d.getCategory().isBlank())
                                ? "General Practitioner"
                                : d.getCategory().trim();
                        return cat.equalsIgnoreCase(spec);
                    })
                    .collect(Collectors.toList());
        }

        // Date filter: keep only doctors who have ≥1 OPEN slot on the requested date
        if (date != null) {
            Set<Long> available = new HashSet<>(
                    slotRepository.findDoctorIdsWithOpenSlotsOnDate(date));
            doctors = doctors.stream()
                    .filter(d -> available.contains(d.getId()))
                    .collect(Collectors.toList());
        }

        return doctors.stream()
                .map(d -> toDTO(d, d.getUser()))
                .collect(Collectors.toList());
    }

    // ── UPDATE by email ───────────────────────────────────────────────
    public void updateDoctorByEmail(String email, DoctorDTO dto, byte[] imageBytes) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found");

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        doctor.setBirthDate(dto.getBirthDate());
        doctor.setGender(dto.getGender());
        doctor.setCategory(dto.getCategory());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setExperience(dto.getExperience());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setContactNumber(dto.getContactNumber());
        doctor.setBio(dto.getBio());

        if (imageBytes != null && imageBytes.length > 0) {
            doctor.setImageLink(Base64.getEncoder().encodeToString(imageBytes));
        }

        userRepository.save(user);
        doctorRepository.save(doctor);
    }
}
