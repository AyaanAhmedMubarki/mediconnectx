package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Assistance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssistanceRepository extends JpaRepository<Assistance, Long> {

    // Find by doctorId and patientId
    Optional<Assistance> findByDoctorIdAndPatientId(Long doctorId, Long patientId);


    List<Assistance> findByPatientId(Long patientId); // Find by patientId

    List<Assistance> findByDoctorId(Long doctorId); // Find by doctorId
}

