package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByNameContainingIgnoreCase(String name);
    List<Medicine> findByType(String type);
    List<Medicine> findByManufacturer(String manufacturer);
}
