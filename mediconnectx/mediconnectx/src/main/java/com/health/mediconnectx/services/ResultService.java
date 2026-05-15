package com.health.mediconnectx.services;


import com.health.mediconnectx.repository.PatientRepository;
import com.health.mediconnectx.repository.EventRepository;
import com.health.mediconnectx.repository.ResultRepository;
import com.health.mediconnectx.dto.ResultDTO;
import com.health.mediconnectx.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResultService {

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PatientRepository patientRepository;

    public Optional<Result> findByPatientIDAndEventId(Long patientId, Long eventId) {
        return resultRepository.findByPatientIdAndEventId(patientId, eventId);
    }

    public List<Result> findByPatientId(Long patientId) {
        return resultRepository.findByPatientId(patientId);
    }

    public List<Result> findByEventId(Long eventId) {
        return resultRepository.findByEventId(eventId);
    }

    public void createResult(ResultDTO resultDTOObject) {
        Result result = new Result();
        result.setPatientId(resultDTOObject.getPatientId());
        result.setEventId(resultDTOObject.getEventId());
        result.setPublishDate(resultDTOObject.getPublishDate());
        result.setScore(resultDTOObject.getScore());
        result.setRemarks(resultDTOObject.getRemarks());

        resultRepository.save(result);
    }

    public List<Result> getAllResult() {
        //get All registrations
        return resultRepository.findAll();
    }
}

