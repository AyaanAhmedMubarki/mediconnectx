package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.AssistanceService;
import com.health.mediconnectx.dto.AssistanceDTO;
import com.health.mediconnectx.entity.Assistance;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assistance")
public class AssistanceController {

    @Autowired
    private AssistanceService assistanceService;

    @GetMapping("/search")
    public ResponseEntity<?> getAssistance(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId) {

        if (doctorId != null && patientId != null) {
            // Search by both doctorId and patientId
            Optional<Assistance> assistance = assistanceService.findByDoctorIdAndPatientId(doctorId, patientId);
            return assistance.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } else if (doctorId != null) {
            // Search by doctorId only
            List<Assistance> assistance = assistanceService.findByDoctorId(doctorId);
            return ResponseEntity.ok(assistance);
        } else if (patientId != null) {
            // Search by patientId only
            List<Assistance> assistance = assistanceService.findByPatientId(patientId);
            return ResponseEntity.ok(assistance);
        } else {
            return ResponseEntity.badRequest().body("Either doctorId or patientId must be provided.");
        }
    }

    // POST: Request a new assistance
    @PostMapping("/request")
    public ResponseEntity<?> createAssistance(@RequestParam("assistanceDTO") String assistanceDTO){

        // Convert JSON string to AssistanceDTO
        ObjectMapper objectMapper = new ObjectMapper();
        AssistanceDTO assistanceDTOObject;
        try {
            assistanceDTOObject = objectMapper.readValue(assistanceDTO, AssistanceDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing assistanceDTO: " + e.getMessage());
        }

        assistanceService.createAssistance(assistanceDTOObject);

        return ResponseEntity.ok("Assistance request Successful!");
    }

    // PUT: Update Assistance status
    @PutMapping("/update")
    public ResponseEntity<?> updateAssistance(@RequestParam Long id, @RequestParam String status, @RequestParam String remarks) {

        // Pass the DTO to the service for updating the Assistance
        assistanceService.updateAssistance(id, status, remarks);

        return ResponseEntity.ok("Assistance update successful");

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAssistance(@PathVariable Long id){
        try {
            assistanceService.deleteAssistance(id);
            return ResponseEntity.ok("Assistance request cancelled successfully");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No request to delete");
        }
    }
}

