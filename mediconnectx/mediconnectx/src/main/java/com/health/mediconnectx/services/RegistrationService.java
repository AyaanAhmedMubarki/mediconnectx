package com.health.mediconnectx.services;

import com.health.mediconnectx.exception.ApiException;
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

    //get All registrations
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
    public Registration createRegistration(RegistrationDTO registrationDTOObject) {
        Long patientId = registrationDTOObject.getPatientId();
        Long eventId   = registrationDTOObject.getEventId();

        // Normalise the role once — used for both the duplicate check and the new row.
        // We MUST include userRole in the duplicate check so that a PATIENT and a DOCTOR
        // who share the same numeric profile ID (both tables auto-increment from 1) do
        // not accidentally receive each other's registration record.
        String normalizedRole = (registrationDTOObject.getUserRole() != null)
                ? registrationDTOObject.getUserRole().toUpperCase()
                : "PATIENT";

        if (patientId != null && eventId != null) {
            Optional<Registration> existing =
                    registrationRepository.findByEventIdAndPatientIdAndUserRole(eventId, patientId, normalizedRole);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        Registration registration = new Registration();
        registration.setPatientId(patientId);
        registration.setEventId(eventId);
        registration.setRegistrationDate(registrationDTOObject.getRegistrationDate());
        registration.setStatus("PENDING");
        registration.setUserRole(normalizedRole);   // reuse already-computed value, no duplicate declaration
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

    // Find a single registration by its primary key
    public Optional<Registration> findById(Long id) {
        return registrationRepository.findById(id);
    }

    // Find registration by eventId and patientId
    public Optional<Registration> findByEventIdAndPatientId(Long eventId, Long patientId) {
        return registrationRepository.findByEventIdAndPatientId(eventId, patientId);
    }

    // Find all registrations for a given profile ID (any role)
    public List<Registration> findByPatientId(Long patientId) {
        return registrationRepository.findByPatientId(patientId);
    }

    // Find all registrations for a given profile ID filtered by role (PATIENT or DOCTOR)
    public List<Registration> findByPatientIdAndUserRole(Long patientId, String userRole) {
        return registrationRepository.findByPatientIdAndUserRole(patientId, userRole);
    }

    // Find all registrations for a specific event
    public List<Registration> findByEventId(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }
}