package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.OfflineSlot;
import com.health.mediconnectx.entity.Doctor;
import com.health.mediconnectx.entity.DoctorLocation;
import com.health.mediconnectx.service.OfflineSlotService;
import com.health.mediconnectx.service.DoctorLocationService;
import com.health.mediconnectx.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/offline-slots")
@CrossOrigin("*")
public class OfflineSlotController {

    @Autowired
    private OfflineSlotService slotService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorLocationService locationService;

    @GetMapping
    public List<OfflineSlot> getAllSlots() {
        return slotService.getAllSlots();
    }

    @PostMapping("/create")
    public OfflineSlot createSlot(
            @RequestParam Long doctorId,
            @RequestParam Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        DoctorLocation location = locationService.getLocationById(locationId);

        if (doctor != null && location != null) {
            return slotService.createSlot(doctor, location, slotDate, startTime, endTime);
        }
        return null;
    }

    @GetMapping("/doctor/{doctorId}")
    public List<OfflineSlot> getSlotsByDoctor(@PathVariable Long doctorId) {
        return slotService.getSlotsByDoctor(doctorId);
    }

    @GetMapping("/doctor/{doctorId}/date")
    public List<OfflineSlot> getSlotsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate) {
        return slotService.getSlotsByDoctorAndDate(doctorId, slotDate);
    }

    @GetMapping("/doctor/{doctorId}/available")
    public List<OfflineSlot> getAvailableSlotsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate) {
        return slotService.getAvailableSlotsByDoctorAndDate(doctorId, slotDate);
    }

    @GetMapping("/available")
    public List<OfflineSlot> getAvailableSlotsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate) {
        return slotService.getAvailableSlotsByDate(slotDate);
    }

    @GetMapping("/{id}")
    public OfflineSlot getSlotById(@PathVariable Long id) {
        return slotService.getSlotById(id);
    }

    @PostMapping("/{slotId}/book")
    public OfflineSlot bookSlot(@PathVariable Long slotId, @RequestParam Long patientId) {
        return slotService.bookSlot(slotId, patientId);
    }

    @DeleteMapping("/{id}")
    public void deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
    }

    @GetMapping("/location/{locationId}")
    public List<OfflineSlot> getSlotsByLocation(@PathVariable Long locationId) {
        return slotService.getSlotsByLocation(locationId);
    }

    @GetMapping("/patient/{patientId}")
    public List<OfflineSlot> getSlotsByPatient(@PathVariable Long patientId) {
        return slotService.getSlotsByPatient(patientId);
    }
}
