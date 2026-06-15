package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.Test;
import com.health.mediconnectx.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin("*")
public class TestController {

    @Autowired
    private TestService service;

    @PostMapping("/create")
    public Test createTest(@RequestBody Test test) {
        return service.createTest(test);
    }

    @GetMapping("/{id}")
    public Test getTest(@PathVariable Long id) {
        return service.getTestById(id);
    }

    @GetMapping("/patient/{patientId}")
    public List<Test> getTestsByPatient(@PathVariable Long patientId) {
        return service.getTestsByPatientId(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Test> getTestsByDoctor(@PathVariable Long doctorId) {
        return service.getTestsByDoctorId(doctorId);
    }

    @GetMapping("/status/{status}")
    public List<Test> getTestsByStatus(@PathVariable String status) {
        return service.getTestsByStatus(status);
    }

    @GetMapping("/type/{testType}")
    public List<Test> getTestsByType(@PathVariable String testType) {
        return service.getTestsByType(testType);
    }

    @GetMapping("/patient/{patientId}/pending")
    public List<Test> getPendingTests(@PathVariable Long patientId) {
        return service.getPendingTestsByPatient(patientId);
    }

    @PutMapping("/{id}")
    public Test updateTest(@PathVariable Long id, @RequestBody Test test) {
        return service.updateTest(id, test);
    }

    @DeleteMapping("/{id}")
    public void deleteTest(@PathVariable Long id) {
        service.deleteTest(id);
    }
}
