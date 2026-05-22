package com.health.mediconnectx.services;

import com.health.mediconnectx.exception.ApiException;
import com.health.mediconnectx.repository.PatientRepository;
import com.health.mediconnectx.repository.EventRepository;
import com.health.mediconnectx.repository.RegistrationRepository;
import com.health.mediconnectx.dto.RegistrationDTO;
import com.health.mediconnectx.entity.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EventRepository eventRepository;

    //get All registrations
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
    public Registration createRegistration(RegistrationDTO registrationDTOObject) {
        Registration registration = new Registration();
        registration.setPatientId(registrationDTOObject.getPatientId());
        registration.setEventId(registrationDTOObject.getEventId());
        registration.setRegistrationDate(registrationDTOObject.getRegistrationDate());
        registration.setStatus("PENDING");
        // Default to PATIENT for backwards-compatibility with existing rows that have no role
        String role = registrationDTOObject.getUserRole();
        registration.setUserRole(role != null ? role.toUpperCase() : "PATIENT");
        return registrationRepository.save(registration);
    }


    public void updateRegistration(Long id, String status) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Registration not found with id: " + id));

        String current = registration.getStatus();
        if ("APPROVED".equals(current) || "REJECTED".equals(current)) {
            throw new ApiException("Registration is already " + current + " and cannot be changed.");
        }

        registration.setStatus(status);
        registrationRepository.save(registration);
    }

    public void deleteRegistration(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Registration not found with id: " + id));

        if (!"PENDING".equals(registration.getStatus())) {
            throw new ApiException(
                "Cannot unregister: your registration has already been " +
                registration.getStatus().toLowerCase() + "."
            );
        }

        registrationRepository.delete(registration);
    }

    // Find registration by eventId and patientId
    public Optional<Registration> findByEventIdAndPatientId(Long eventId, Long patientId) {
        return registrationRepository.findByEventIdAndPatientId(eventId, patientId);
    }

    // Find all registrations for a specific athlete
    public List<Registration> findByPatientId(Long patientId) {
        return registrationRepository.findByPatientId(patientId);
    }

    // Find all registrations for a specific event
    public List<Registration> findByEventId(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }
}