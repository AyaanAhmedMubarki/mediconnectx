package com.health.mediconnectx.services;

import com.health.mediconnectx.entity.AppointmentSlot;
import com.health.mediconnectx.entity.DoctorShift;
import com.health.mediconnectx.entity.SlotStatus;
import com.health.mediconnectx.repository.AppointmentSlotRepository;
import com.health.mediconnectx.repository.DoctorShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a doctor's date-specific shift templates into 15-minute AppointmentSlot rows.
 *
 * Algorithm:
 *   For each DoctorShift belonging to the doctor:
 *     - Skip if shiftDate is null (legacy rows) or in the past
 *     - Skip if slots already exist for that doctor + date (idempotency guard)
 *     - Walk startTime → endTime in 15-minute steps, creating one OPEN slot per step
 *     - Batch-insert via saveAll()
 *
 * This design is intentionally simple: no day-of-week matching, no date iteration.
 * The doctor is in full control — they add exactly the dates they are available,
 * and this service stamps out the individual booking slots.
 */
@Service
public class SlotGenerationService {

    private static final int SLOT_DURATION_MINUTES = 15;

    @Autowired
    private DoctorShiftRepository doctorShiftRepository;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    /**
     * Generates 15-minute AppointmentSlot rows for ONE specific shift.
     * Called immediately after a new shift is saved (from DoctorShiftController)
     * so that slots are always created even when other shifts on the same date
     * already have slots — the broad date-level guard in generateSlotsForDoctor()
     * would silently skip this shift in that scenario.
     *
     * Idempotency: checks whether a slot at exactly shiftStartTime already exists;
     * if it does, the generation is skipped (safe to call multiple times).
     *
     * @return Number of new slots inserted (0 if already generated or past date)
     */
    @Transactional
    public int generateSlotsForShift(Long doctorId,
                                     LocalDate shiftDate,
                                     LocalTime startTime,
                                     LocalTime endTime) {
        if (shiftDate == null) return 0;
        if (shiftDate.isBefore(LocalDate.now())) return 0;

        // Per-shift idempotency: if the first slot already exists, skip.
        if (appointmentSlotRepository.existsByDoctorIdAndSlotDateAndStartTime(
                doctorId, shiftDate, startTime)) {
            return 0;
        }

        List<AppointmentSlot> slotsToSave = new ArrayList<>();
        LocalTime cursor = startTime;

        while (!cursor.plusMinutes(SLOT_DURATION_MINUTES).isAfter(endTime)) {
            AppointmentSlot slot = new AppointmentSlot();
            slot.setDoctorId(doctorId);
            slot.setSlotDate(shiftDate);
            slot.setStartTime(cursor);
            slot.setEndTime(cursor.plusMinutes(SLOT_DURATION_MINUTES));
            slot.setStatus(SlotStatus.OPEN);
            slotsToSave.add(slot);
            cursor = cursor.plusMinutes(SLOT_DURATION_MINUTES);
        }

        if (!slotsToSave.isEmpty()) {
            appointmentSlotRepository.saveAll(slotsToSave);
        }
        return slotsToSave.size();
    }

    /**
     * Generates 15-minute AppointmentSlot rows for all of this doctor's future shifts
     * that do not already have slots.
     *
     * @param doctorId The Doctor entity ID
     * @return Total number of new slots inserted
     */
    @Transactional
    public int generateSlotsForDoctor(Long doctorId) {
        int totalCreated = 0;
        LocalDate today  = LocalDate.now();

        List<DoctorShift> shifts = doctorShiftRepository.findByDoctorId(doctorId);

        for (DoctorShift shift : shifts) {
            LocalDate shiftDate = shift.getShiftDate();

            // Skip legacy rows that pre-date the calendar model
            if (shiftDate == null) continue;

            // Skip past dates (already elapsed, no point generating)
            if (shiftDate.isBefore(today)) continue;

            // Per-shift idempotency: if a slot at this exact start time already exists,
            // this shift was already generated — skip it.
            // (Using startTime rather than a date-level check so that multiple shifts
            // on the same date are each generated independently.)
            if (appointmentSlotRepository.existsByDoctorIdAndSlotDateAndStartTime(
                    doctorId, shiftDate, shift.getStartTime())) continue;

            List<AppointmentSlot> slotsToSave = new ArrayList<>();
            LocalTime cursor = shift.getStartTime();

            // Walk from shift start to shift end in 15-minute steps.
            // A slot is valid only if its END fits within the shift END.
            while (!cursor.plusMinutes(SLOT_DURATION_MINUTES).isAfter(shift.getEndTime())) {
                AppointmentSlot slot = new AppointmentSlot();
                slot.setDoctorId(doctorId);
                slot.setSlotDate(shiftDate);
                slot.setStartTime(cursor);
                slot.setEndTime(cursor.plusMinutes(SLOT_DURATION_MINUTES));
                slot.setStatus(SlotStatus.OPEN);
                slotsToSave.add(slot);

                cursor = cursor.plusMinutes(SLOT_DURATION_MINUTES);
            }

            if (!slotsToSave.isEmpty()) {
                appointmentSlotRepository.saveAll(slotsToSave);
                totalCreated += slotsToSave.size();
            }
        }

        return totalCreated;
    }
}
