package com.health.mediconnectx.service;

import com.health.mediconnectx.entity.OfflineSlot;
import com.health.mediconnectx.entity.Doctor;
import com.health.mediconnectx.entity.DoctorLocation;
import com.health.mediconnectx.entity.Patient;
import com.health.mediconnectx.repository.OfflineSlotRepository;
import com.health.mediconnectx.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class OfflineSlotService {

    @Autowired
    private OfflineSlotRepository repository;

    @Autowired
    private PatientRepository patientRepository;

    public List<OfflineSlot> getAllSlots() {
        return repository.findAll();
    }

    public OfflineSlot createSlot(Doctor doctor, DoctorLocation location, LocalDate slotDate, LocalTime startTime, LocalTime endTime) {
        OfflineSlot slot = new OfflineSlot(doctor, location, slotDate, startTime, endTime);
        return repository.save(slot);
    }

    public List<OfflineSlot> getSlotsByDoctorAndDate(Long doctorId, LocalDate slotDate) {
        return repository.findByDoctorIdAndSlotDate(doctorId, slotDate);
    }

    public List<OfflineSlot> getSlotsByDoctor(Long doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public List<OfflineSlot> getAvailableSlotsByDoctorAndDate(Long doctorId, LocalDate slotDate) {
        return repository.findByDoctorIdAndSlotDateAndStatus(doctorId, slotDate, OfflineSlot.SlotStatus.OPEN);
    }

    public List<OfflineSlot> getAvailableSlotsByDate(LocalDate slotDate) {
        return repository.findBySlotDateAndStatus(slotDate, OfflineSlot.SlotStatus.OPEN);
    }

    public OfflineSlot bookSlot(Long slotId, Long patientId) {
        OfflineSlot slot = repository.findById(slotId).orElse(null);
        if (slot != null && slot.getStatus() == OfflineSlot.SlotStatus.OPEN) {
            slot.setStatus(OfflineSlot.SlotStatus.BOOKED);
            Patient patient = patientRepository.findById(patientId).orElse(null);
            if (patient != null) {
                slot.setPatient(patient);
            }
            return repository.save(slot);
        }
        return null;
    }

    public OfflineSlot getSlotById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteSlot(Long id) {
        repository.deleteById(id);
    }

    public List<OfflineSlot> getSlotsByLocation(Long locationId) {
        return repository.findByLocationId(locationId);
    }

    public List<OfflineSlot> getSlotsByPatient(Long patientId) {
        return repository.findByPatientId(patientId);
    }
}
