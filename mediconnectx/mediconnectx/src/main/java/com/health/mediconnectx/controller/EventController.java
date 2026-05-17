package com.health.mediconnectx.controller;

import com.health.mediconnectx.dto.AdminDTO;
import com.health.mediconnectx.dto.EventDTO;
import com.health.mediconnectx.entity.Event;
import com.health.mediconnectx.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events available currently");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestParam("eventDTO") String eventDTO, @RequestParam(value = "imageLink", required = false) MultipartFile imageLink){

        // Convert JSON string to AdminDTO
        ObjectMapper objectMapper = new ObjectMapper();
        EventDTO eventDTOObject;
        try {
            eventDTOObject = objectMapper.readValue(eventDTO, EventDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing eventDTO: " + e.getMessage());
        }

        byte[] imageBytes = null;

        // If a profile image is uploaded, convert it to byte[] for processing
        if (imageLink != null && !imageLink.isEmpty()) {
            try {
                imageBytes = imageLink.getBytes(); // Convert the image to byte[] format
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
            }
        }

        // Pass the DTO and image bytes to the service for updating the profile
        eventService.createEvent(eventDTOObject, imageBytes);

        return ResponseEntity.ok("Event Creation Successful!");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteEvent(@RequestParam Long id){
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok("Event deletion successful");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No event to delete");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateEvent(@RequestParam("eventDTO") String eventDTO, @RequestParam(value = "imageLink", required = false) MultipartFile imageLink) {
        // Convert JSON string to AdminDTO
        ObjectMapper objectMapper = new ObjectMapper();
        EventDTO eventDTOObject;
        try {
            eventDTOObject = objectMapper.readValue(eventDTO, EventDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error parsing eventDTO: " + e.getMessage());
        }

        byte[] imageBytes = null;

        // If a profile image is uploaded, convert it to byte[] for processing
        if (imageLink != null && !imageLink.isEmpty()) {
            try {
                imageBytes = imageLink.getBytes(); // Convert the image to byte[] format
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
            }
        }

        // Pass the DTO and image bytes to the service for updating the profile
        eventService.updateEvent(eventDTOObject, imageBytes);

        return ResponseEntity.ok("Event update successful");

    }

    @GetMapping("/search")
    public ResponseEntity<?> getEventById(@RequestParam Long id) {
        try {
            Event event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events available currently");
        }
    }
}
