package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Find by eventId and patientId
    Optional<Registration> findByEventIdAndPatientId(Long eventId, Long patientId);


    List<Registration> findByPatientId(Long patientId); // Find by patientId

    List<Registration> findByEventId(Long eventId); // Find by eventId
}
