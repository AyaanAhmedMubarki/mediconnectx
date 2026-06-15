package com.health.mediconnectx.service;

import com.health.mediconnectx.entity.Test;
import com.health.mediconnectx.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TestService {

    @Autowired
    private TestRepository repository;

    public Test createTest(Test test) {
        return repository.save(test);
    }

    public Test getTestById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Test> getTestsByPatientId(Long patientId) {
        return repository.findByPatientId(patientId);
    }

    public List<Test> getTestsByDoctorId(Long doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public List<Test> getTestsByStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<Test> getTestsByType(String testType) {
        return repository.findByTestType(testType);
    }

    public List<Test> getPendingTestsByPatient(Long patientId) {
        return repository.findByPatientIdAndStatus(patientId, "PENDING");
    }

    public Test updateTest(Long id, Test test) {
        Test existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setTestName(test.getTestName());
            existing.setTestType(test.getTestType());
            existing.setTestDescription(test.getTestDescription());
            existing.setTestDate(test.getTestDate());
            existing.setResults(test.getResults());
            existing.setStatus(test.getStatus());
            existing.setNormalRange(test.getNormalRange());
            return repository.save(existing);
        }
        return null;
    }

    public void deleteTest(Long id) {
        repository.deleteById(id);
    }
}
