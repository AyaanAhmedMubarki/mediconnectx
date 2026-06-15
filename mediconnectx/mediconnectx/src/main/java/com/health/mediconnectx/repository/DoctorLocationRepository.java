package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.DoctorLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorLocationRepository extends JpaRepository<DoctorLocation, Long> {
    List<DoctorLocation> findByDoctorId(Long doctorId);
    List<DoctorLocation> findByCity(String city);
}
