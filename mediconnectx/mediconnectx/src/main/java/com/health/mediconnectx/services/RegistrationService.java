package com.health.mediconnectx.services;

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
    public void createRegistration (RegistrationDTO registrationDTOObject){

        Registration registration = new Registration();
        registration.setPatientId(registrationDTOObject.getPatientId());
        registration.setEventId(registrationDTOObject.getEventId());
        registration.setRegistrationDate(registrationDTOObject.getRegistrationDate());
        registration.setStatus("PENDING");

        registrationRepository.save(registration);
    }


    public void updateRegistration(Long id, String status) {

        Registration registration = registrationRepository.getReferenceById(id);
        registration.setStatus(status);
        registrationRepository.save(registration);
    }

    public void deleteRegistration(Long id) {
        Registration registration = registrationRepository.getReferenceById(id);
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