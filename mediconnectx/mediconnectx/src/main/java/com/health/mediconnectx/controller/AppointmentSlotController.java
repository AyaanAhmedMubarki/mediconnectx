package com.health.mediconnectx.controller;

import com.health.mediconnectx.dto.BookingRequestDTO;
import com.health.mediconnectx.dto.SlotDTO;
import com.health.mediconnectx.entity.AppointmentSlot;
import com.health.mediconnectx.entity.SlotStatus;
import com.health.mediconnectx.exception.SlotAlreadyTakenException;
import com.health.mediconnectx.repository.AppointmentSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for the patient-facing slot browsing and booking flow.
 *
 * Endpoints:
 *   GET  /api/v1/slots?doctorId=X&date=yyyy-MM-dd  — list slots for a doctor on a date
 *   POST /api/v1/slots/{slotId}/book                — book a slot (returns 409 if taken)
 */
@RestController
@RequestMapping("/api/v1/slots")
@CrossOrigin(origins = "*")
public class AppointmentSlotController {

    @Autowired
    private AppointmentSlotRepository slotRepository;

    /**
     * Returns all pre-generated slots for the given doctor on the given date,
     * ordered by start time. Includes OPEN, PENDING_PAYMENT, and BOOKED slots
     * so the patient can see which times are taken.
     */
    @GetMapping
    public ResponseEntity<List<SlotDTO>> getSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Past dates: nothing to show — return empty list
        if (date.isBefore(LocalDate.now())) {
            return ResponseEntity.ok(List.of());
        }

        // For today (and future dates): return all slots as-is.
        // The frontend marks past-time slots as non-clickable "Past" buttons
        // so the patient can see the full shift picture for the day.
        List<SlotDTO> slots = slotRepository
                .findByDoctorIdAndSlotDateOrderByStartTimeAsc(doctorId, date)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(slots);
    }

    /**
     * Books a slot atomically. Uses a conditional UPDATE (WHERE status = OPEN)
     * so only one concurrent request can succeed.
     *
     * Returns 200 with the updated slot on success.
     * Returns 409 Conflict (via GlobalExceptionHandler) if the slot is already taken.
     */
    @PostMapping("/{slotId}/book")
    @Transactional
    public ResponseEntity<SlotDTO> bookSlot(@PathVariable Long slotId,
                                             @RequestBody BookingRequestDTO request) {

        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required.");
        }

        // Atomic conditional update — returns 0 if slot is not OPEN
        int updated = slotRepository.safeBookSlot(
                slotId,
                request.getPatientId(),
                SlotStatus.BOOKED,
                SlotStatus.OPEN
        );

        if (updated == 0) {
            throw new SlotAlreadyTakenException(slotId);
        }

        // clearAutomatically = true on the repo query flushes the cache,
        // so this findById returns the freshly-updated slot.
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot " + slotId + " not found after booking."));

        return ResponseEntity.ok(toDTO(slot));
    }

    // ── Mapping ──────────────────────────────────────────────────

    private SlotDTO toDTO(AppointmentSlot slot) {
        return new SlotDTO(
                slot.getId(),
                slot.getDoctorId(),
                slot.getSlotDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }
}
