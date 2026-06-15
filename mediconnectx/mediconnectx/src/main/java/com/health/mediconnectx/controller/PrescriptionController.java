package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.Prescription;
import com.health.mediconnectx.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin("*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService service;

    @PostMapping("/create")
    public Prescription createPrescription(@RequestBody Prescription prescription) {
        return service.createPrescription(prescription);
    }

    @GetMapping("/{id}")
    public Prescription getPrescription(@PathVariable Long id) {
        return service.getPrescriptionById(id);
    }

    @GetMapping("/appointment/{appointmentId}")
    public List<Prescription> getPrescriptionsByAppointment(@PathVariable Long appointmentId) {
        return service.getPrescriptionsByAppointmentId(appointmentId);
    }

    @GetMapping("/patient/{patientId}")
    public List<Prescription> getPrescriptionsByPatient(@PathVariable Long patientId) {
        return service.getPrescriptionsByPatientId(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Prescription> getPrescriptionsByDoctor(@PathVariable Long doctorId) {
        return service.getPrescriptionsByDoctorId(doctorId);
    }

    @GetMapping("/status/{status}")
    public List<Prescription> getPrescriptionsByStatus(@PathVariable String status) {
        return service.getPrescriptionsByStatus(status);
    }

    @PutMapping("/{id}")
    public Prescription updatePrescription(@PathVariable Long id, @RequestBody Prescription prescription) {
        return service.updatePrescription(id, prescription);
    }

    @DeleteMapping("/{id}")
    public void deletePrescription(@PathVariable Long id) {
        service.deletePrescription(id);
    }
}
