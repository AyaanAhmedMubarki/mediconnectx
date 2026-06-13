package com.health.mediconnectx.services;

import com.health.mediconnectx.dto.ShiftDTO;
import com.health.mediconnectx.entity.DoctorShift;
import com.health.mediconnectx.repository.AppointmentSlotRepository;
import com.health.mediconnectx.repository.DoctorShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorShiftService {

    @Autowired
    private DoctorShiftRepository doctorShiftRepository;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    /** Save a new date-specific shift template for a doctor. */
    @Transactional
    public ShiftDTO createShift(Long doctorId, ShiftDTO dto) {
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new IllegalArgumentException("startTime and endTime are required.");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime.");
        }
        if (dto.getShiftDate() == null) {
            throw new IllegalArgumentException("shiftDate is required.");
        }
        if (dto.getShiftDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("shiftDate cannot be in the past.");
        }
        // If the shift is for today, its end time must still be in the future
        if (dto.getShiftDate().isEqual(LocalDate.now()) &&
                !dto.getEndTime().isAfter(LocalTime.now())) {
            throw new IllegalArgumentException("End time cannot be in the past for today's shifts.");
        }

        DoctorShift shift = new DoctorShift();
        shift.setDoctorId(doctorId);
        shift.setShiftDate(dto.getShiftDate());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());

        return toDTO(doctorShiftRepository.save(shift));
    }

    /**
     * Returns only current and future shift templates for a given doctor, sorted by date then start time.
     *
     * Filtering rules:
     *   - shiftDate > today          → always included (future date)
     *   - shiftDate == today         → included only if endTime is still in the future
     *                                  (e.g. a 09:00-13:00 shift is hidden after 13:00)
     *   - shiftDate < today          → excluded (past date)
     *   - shiftDate == null          → excluded (legacy rows)
     */
    public List<ShiftDTO> getShiftsForDoctor(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        return doctorShiftRepository.findByDoctorId(doctorId)
                .stream()
                .filter(s -> {
                    if (s.getShiftDate() == null) return false;
                    if (s.getShiftDate().isAfter(today))  return true;   // future date — always show
                    if (s.getShiftDate().isEqual(today))  {              // today — show only if not over yet
                        return s.getEndTime().isAfter(now);
                    }
                    return false;                                          // past date — hide
                })
                .sorted((a, b) -> {
                    int dateCmp = a.getShiftDate().compareTo(b.getShiftDate());
                    if (dateCmp != 0) return dateCmp;
                    return a.getStartTime().compareTo(b.getStartTime());
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete a single shift by ID.
     * When a shift is deleted, all OPEN (unbooked) slots within its timeframe are also deleted.
     * Booked slots are preserved so that patients' confirmed appointments are not affected.
     * Returns the number of unbooked slots that were deleted.
     */
    @Transactional
    public int deleteShift(Long shiftId) {
        Optional<DoctorShift> shiftOpt = doctorShiftRepository.findById(shiftId);
        if (shiftOpt.isEmpty()) {
            throw new IllegalArgumentException("Shift " + shiftId + " not found.");
        }

        DoctorShift shift = shiftOpt.get();

        // Delete all OPEN slots within this shift's timeframe on this date
        int deletedSlots = appointmentSlotRepository.deleteOpenSlotsInTimeRange(
                shift.getDoctorId(),
                shift.getShiftDate(),
                shift.getStartTime(),
                shift.getEndTime()
        );

        // Delete the shift template itself
        doctorShiftRepository.deleteById(shiftId);

        return deletedSlots;
    }

    // ── Mapping ──────────────────────────────────────────────────

    private ShiftDTO toDTO(DoctorShift shift) {
        return new ShiftDTO(
                shift.getId(),
                shift.getDoctorId(),
                shift.getShiftDate(),
                shift.getStartTime(),
                shift.getEndTime()
        );
    }
}
