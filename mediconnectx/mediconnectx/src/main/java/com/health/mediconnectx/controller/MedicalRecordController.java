package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.MedicalRecord;
import com.health.mediconnectx.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin("*")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService service;

    @PostMapping("/create")
    public MedicalRecord createRecord(@RequestBody MedicalRecord record) {
        return service.createRecord(record);
    }

    @GetMapping("/{id}")
    public MedicalRecord getRecord(@PathVariable Long id) {
        return service.getRecordById(id);
    }

    @GetMapping("/appointment/{appointmentId}")
    public MedicalRecord getRecordByAppointment(@PathVariable Long appointmentId) {
        return service.getRecordByAppointmentId(appointmentId);
    }

    @GetMapping("/patient/{patientId}")
    public List<MedicalRecord> getRecordsByPatient(@PathVariable Long patientId) {
        return service.getRecordsByPatientId(patientId);
    }

    @PutMapping("/{id}")
    public MedicalRecord updateRecord(@PathVariable Long id, @RequestBody MedicalRecord record) {
        return service.updateRecord(id, record);
    }

    @DeleteMapping("/{id}")
    public void deleteRecord(@PathVariable Long id) {
        service.deleteRecord(id);
    }
}
