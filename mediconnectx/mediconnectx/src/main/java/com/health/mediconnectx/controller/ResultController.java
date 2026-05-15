package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.ResultService;
import com.health.mediconnectx.dto.ResultDTO;
import com.health.mediconnectx.entity.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/result")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping("/search")
    public ResponseEntity<?> getResult(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long eventId) {

        if (patientId != null && eventId != null) {
            // Search by both eventId and patientId
            Optional<Result> result = resultService.findByPatientIDAndEventId(patientId, eventId);
            return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } else if (patientId != null) {
            // Search by patientId only
            List<Result> result = resultService.findByPatientId(patientId);
            return ResponseEntity.ok(result);
        } else if (eventId != null) {
            // Search by eventId only
            List<Result> result = resultService.findByEventId(eventId);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body("Either patientId or eventId must be provided.");
        }
    }

    // POST: publish results
    @PostMapping("/publish")
    public ResponseEntity<?> createResult(@RequestParam("resultDTO") String resultDTO){

        // Convert JSON string to ResultDTO
        ObjectMapper objectMapper = new ObjectMapper();
        ResultDTO resultDTOObject;
        try {
            resultDTOObject = objectMapper.readValue(resultDTO, ResultDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing resultDTO: " + e.getMessage());
        }

        resultService.createResult(resultDTOObject);

        return ResponseEntity.ok("Result publishing Successful!");
    }

    // GET: Fetch all registrations
    @GetMapping("/all")
    public ResponseEntity<?> getAllResult() {
        try {
            List<Result> result = resultService.getAllResult();
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No results available currently");
        }
    }
}

