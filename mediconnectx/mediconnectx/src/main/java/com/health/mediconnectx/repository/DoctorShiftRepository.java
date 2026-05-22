package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.DoctorShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorShiftRepository extends JpaRepository<DoctorShift, Long> {

    /** Returns all shift templates for a given doctor. */
    List<DoctorShift> findByDoctorId(Long doctorId);
}
