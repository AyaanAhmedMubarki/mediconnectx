package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    // Find by eventId and patientId
    Optional<Result> findByPatientIdAndEventId(Long patientId, Long eventId);

    List<Result> findByPatientId(Long patientId); // Find by patientId

    List<Result> findByEventId(Long eventId); // Find by eventId
}