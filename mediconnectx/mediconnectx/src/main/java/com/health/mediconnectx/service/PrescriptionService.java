package com.health.mediconnectx.service;

import com.health.mediconnectx.entity.Prescription;
import com.health.mediconnectx.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository repository;

    public Prescription createPrescription(Prescription prescription) {
        return repository.save(prescription);
    }

    public Prescription getPrescriptionById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Prescription> getPrescriptionsByAppointmentId(Long appointmentId) {
        return repository.findByAppointmentId(appointmentId);
    }

    public List<Prescription> getPrescriptionsByPatientId(Long patientId) {
        return repository.findByPatientId(patientId);
    }

    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public List<Prescription> getPrescriptionsByStatus(String status) {
        return repository.findByStatus(status);
    }

    public Prescription updatePrescription(Long id, Prescription prescription) {
        Prescription existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setMedicine(prescription.getMedicine());
            existing.setDosage(prescription.getDosage());
            existing.setFrequency(prescription.getFrequency());
            existing.setDuration(prescription.getDuration());
            existing.setQuantity(prescription.getQuantity());
            existing.setInstructions(prescription.getInstructions());
            existing.setWarnings(prescription.getWarnings());
            existing.setStatus(prescription.getStatus());
            return repository.save(existing);
        }
        return null;
    }

    public void deletePrescription(Long id) {
        repository.deleteById(id);
    }
}
