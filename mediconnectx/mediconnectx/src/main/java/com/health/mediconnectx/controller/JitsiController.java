package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.Appointment;
import com.health.mediconnectx.entity.AppointmentStatus;
import com.health.mediconnectx.repository.AppointmentRepository;
import com.health.mediconnectx.services.JitsiTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class JitsiController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private JitsiTokenService jitsiTokenService;

    @Value("${jitsi.app.id:mediconnectx-demo}")
    private String APP_ID;

    /**
     * Returns Jitsi room details ONLY if the appointment is BOOKED.
     * This is the core security gate — the frontend cannot join a room
     * without a confirmed payment in the database.
     */
    @GetMapping("/get-room")
    public ResponseEntity<Map<String, String>> joinRoom(
            @RequestParam Long appointmentId,
            @RequestParam String currentUserName,
            @RequestParam boolean isDoctor) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        if (appointment == null) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "Appointment not found."));
        }

        // CORE SECURITY: Only BOOKED (paid) appointments get video access
        if (appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "Access Denied: Appointment fee has not been confirmed."));
        }

        // TIME GATE: Allow joining only within 10 minutes of the scheduled appointment time
        if (appointment.getAppointmentTime() != null
                && appointment.getStatus() == AppointmentStatus.BOOKED) {
            LocalDateTime now      = LocalDateTime.now();
            LocalDateTime openTime = appointment.getAppointmentTime().minusMinutes(10);
            if (now.isBefore(openTime)) {
                long minsLeft = Duration.between(now, openTime).toMinutes() + 1;
                Map<String, String> early = new HashMap<>();
                early.put("error",           "TOO_EARLY");
                early.put("message",         "The room opens 10 minutes before your appointment.");
                early.put("openAt",          openTime.toString());           // ISO-8601 for frontend countdown
                early.put("appointmentTime", appointment.getAppointmentTime().toString());
                early.put("minutesLeft",     String.valueOf(minsLeft));
                return ResponseEntity.status(403).body(early);
            }
        }

        String roomName = "mediconnectx-room-" + appointmentId;
        String jwtToken = jitsiTokenService.generateJitsiToken(currentUserName, roomName, isDoctor);

        Map<String, String> data = new HashMap<>();
        data.put("roomName", roomName);
        data.put("token", jwtToken);
        data.put("domain", "meet.jit.si");
        data.put("appId", APP_ID);

        return ResponseEntity.ok(data);
    }
}
