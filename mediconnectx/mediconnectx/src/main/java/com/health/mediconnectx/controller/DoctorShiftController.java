package com.health.mediconnectx.controller;

import com.health.mediconnectx.dto.ShiftDTO;
import com.health.mediconnectx.services.DoctorShiftService;
import com.health.mediconnectx.services.SlotGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing a doctor's weekly shift templates and
 * triggering slot pre-generation.
 *
 * Endpoints:
 *   GET    /api/v1/shifts?doctorId=X          — list all shifts for a doctor
 *   POST   /api/v1/shifts?doctorId=X          — add a new shift
 *   DELETE /api/v1/shifts/{shiftId}            — remove a shift
 *   POST   /api/v1/shifts/generate?doctorId=X&days=30 — generate slots
 */
@RestController
@RequestMapping("/api/v1/shifts")
@CrossOrigin(origins = "*")
public class DoctorShiftController {

    @Autowired
    private DoctorShiftService doctorShiftService;

    @Autowired
    private SlotGenerationService slotGenerationService;

    @GetMapping
    public ResponseEntity<List<ShiftDTO>> getShifts(@RequestParam Long doctorId) {
        return ResponseEntity.ok(doctorShiftService.getShiftsForDoctor(doctorId));
    }

    @PostMapping
    public ResponseEntity<ShiftDTO> createShift(@RequestParam Long doctorId,
                                                 @RequestBody ShiftDTO dto) {
        ShiftDTO created = doctorShiftService.createShift(doctorId, dto);

        // Auto-generate slots for this specific shift immediately.
        // We use generateSlotsForShift() (not generateSlotsForDoctor()) so that
        // the per-date idempotency guard in the broad method does NOT block slot
        // creation when the doctor already has other slots on the same date.
        try {
            int n = slotGenerationService.generateSlotsForShift(
                    doctorId,
                    created.getShiftDate(),
                    created.getStartTime(),
                    created.getEndTime()
            );
            if (n > 0) {
                System.out.printf("[DoctorShiftController] Auto-generated %d slot(s) for doctor %d%n", n, doctorId);
            }
        } catch (Exception e) {
            // Non-fatal — shift is saved; doctor can still trigger generation manually
            System.err.println("[DoctorShiftController] Auto-generation skipped: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{shiftId}")
    public ResponseEntity<Map<String, Object>> deleteShift(@PathVariable Long shiftId) {
        int deletedSlots = doctorShiftService.deleteShift(shiftId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Shift and associated unbooked slots removed.");
        response.put("unbookedSlotsDeleted", deletedSlots);
        return ResponseEntity.ok(response);
    }

    /**
     * Triggers the slot generation engine for the specified doctor.
     * Iterates the doctor's shift templates and creates OPEN AppointmentSlot rows
     * for every future date that does not already have slots.
     * Safe to call multiple times — already-generated dates are skipped.
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSlots(
            @RequestParam Long doctorId) {

        try {
            int created = slotGenerationService.generateSlotsForDoctor(doctorId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message",      "Slot generation complete.");
            resp.put("slotsCreated", created);
            resp.put("doctorId",     doctorId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            // Return a proper JSON error so the frontend can display the actual cause
            Map<String, Object> err = new HashMap<>();
            err.put("error",     e.getClass().getSimpleName() + ": " + e.getMessage());
            err.put("cause",     e.getCause() != null ? e.getCause().getMessage() : "none");
            return ResponseEntity.status(500).body(err);
        }
    }
}
