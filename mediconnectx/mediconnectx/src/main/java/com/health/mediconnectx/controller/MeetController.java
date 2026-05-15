package com.health.mediconnectx.controller;

import com.health.mediconnectx.services.MeetService;
import com.health.mediconnectx.entity.Meet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping("/api/meets")
public class MeetController {

    @Autowired
    private MeetService meetService;

    @GetMapping
    public ResponseEntity<?> getMeet() {
        try {
            List<Meet> meetDetails = meetService.getMeet();
            return ResponseEntity.ok(meetDetails);
        } catch (RuntimeException e) {
            // Return more specific error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Currently no meets going on");
        }
    }

    @PostMapping("/set")
    public ResponseEntity<String> createMeet(@RequestBody Meet meet) {
        try {
            meetService.setMeet(meet);
            return ResponseEntity.ok("Meet created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMeet(@RequestParam Long id){
        meetService.deleteMeet(id);
        return ResponseEntity.ok("Meet deleted successfully");
    }
}

