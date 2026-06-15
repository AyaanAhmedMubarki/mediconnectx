package com.health.mediconnectx.service;

import com.health.mediconnectx.entity.MedicalRecord;
import com.health.mediconnectx.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository repository;

    public MedicalRecord createRecord(MedicalRecord record) {
        return repository.save(record);
    }

    public MedicalRecord getRecordById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public MedicalRecord getRecordByAppointmentId(Long appointmentId) {
        return repository.findByAppointmentId(appointmentId).orElse(null);
    }

    public List<MedicalRecord> getRecordsByPatientId(Long patientId) {
        return repository.findByAppointmentPatientId(patientId);
    }

    public MedicalRecord updateRecord(Long id, MedicalRecord record) {
        MedicalRecord existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setDiagnosis(record.getDiagnosis());
            existing.setNotes(record.getNotes());
            existing.setObservations(record.getObservations());
            return repository.save(existing);
        }
        return null;
    }

    public void deleteRecord(Long id) {
        repository.deleteById(id);
    }
}
