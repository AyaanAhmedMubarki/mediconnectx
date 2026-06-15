package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByPatientId(Long patientId);
    List<Test> findByDoctorId(Long doctorId);
    List<Test> findByStatus(String status);
    List<Test> findByTestType(String testType);
    List<Test> findByPatientIdAndStatus(Long patientId, String status);
}
