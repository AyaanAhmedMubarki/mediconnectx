package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Find by eventId and patientId (used by admin / event-level lookups)
    Optional<Registration> findByEventIdAndPatientId(Long eventId, Long patientId);

    // Find by eventId + patientId + role (used for duplicate-check so a patient and a
    // doctor who share the same numeric ID don't collide)
    Optional<Registration> findByEventIdAndPatientIdAndUserRole(Long eventId, Long patientId, String userRole);


    List<Registration> findByPatientId(Long patientId); // Find by patientId (all roles)

    List<Registration> findByPatientIdAndUserRole(Long patientId, String userRole); // Find by patientId + role

    List<Registration> findByEventId(Long eventId); // Find by eventId
}
