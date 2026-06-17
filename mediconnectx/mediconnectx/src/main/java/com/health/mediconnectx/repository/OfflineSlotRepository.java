package com.health.mediconnectx.repository;

import com.health.mediconnectx.entity.OfflineSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OfflineSlotRepository extends JpaRepository<OfflineSlot, Long> {
    List<OfflineSlot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);
    List<OfflineSlot> findByDoctorId(Long doctorId);
    List<OfflineSlot> findByLocationId(Long locationId);
    List<OfflineSlot> findByDoctorIdAndSlotDateAndStatus(Long doctorId, LocalDate slotDate, OfflineSlot.SlotStatus status);
    List<OfflineSlot> findBySlotDateAndStatus(LocalDate slotDate, OfflineSlot.SlotStatus status);
    List<OfflineSlot> findByPatientId(Long patientId);
}
