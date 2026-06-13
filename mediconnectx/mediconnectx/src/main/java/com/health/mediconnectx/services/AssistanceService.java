package com.health.mediconnectx.services;

import com.health.mediconnectx.repository.AssistanceRepository;
import com.health.mediconnectx.dto.AssistanceDTO;
import com.health.mediconnectx.entity.Assistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssistanceService {

    @Autowired
    private AssistanceRepository assistanceRepository;

    public Optional<Assistance> findByDoctorIdAndPatientId(Long doctorId, Long patientId) {
        return assistanceRepository.findByDoctorIdAndPatientId(doctorId, patientId);
    }

    public List<Assistance> findByDoctorId(Long doctorId) {
        return assistanceRepository.findByDoctorId(doctorId);
    }

    public List<Assistance> findByPatientId(Long patientId) {
        return assistanceRepository.findByPatientId(patientId);
    }

    public void createAssistance(AssistanceDTO assistanceDTOObject) {
        Assistance assistance = new Assistance();
        assistance.setPatientId(assistanceDTOObject.getPatientId());
        assistance.setDoctorId(assistanceDTOObject.getDoctorId());
        assistance.setRequestDate(assistanceDTOObject.getRequestDate());
        assistance.setStatus("PENDING");

        assistanceRepository.save(assistance);
    }

    public void updateAssistance(Long id, String status, String remarks) {
        Assistance assistance = assistanceRepository.getReferenceById(id);
        assistance.setStatus(status);
        assistance.setRemarks(remarks);
        assistanceRepository.save(assistance);
    }

    public void deleteAssistance(Long id) {
        Assistance assistance = assistanceRepository.getReferenceById(id);
        assistanceRepository.delete(assistance);
    }
}

