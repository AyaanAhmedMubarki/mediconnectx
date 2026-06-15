package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.DoctorLocation;
import com.health.mediconnectx.service.DoctorLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-locations")
@CrossOrigin("*")
public class DoctorLocationController {

    @Autowired
    private DoctorLocationService service;

    @PostMapping("/create")
    public DoctorLocation createLocation(@RequestBody DoctorLocation location) {
        return service.createLocation(location);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<DoctorLocation> getLocationsByDoctor(@PathVariable Long doctorId) {
        return service.getLocationsByDoctorId(doctorId);
    }

    @GetMapping("/{id}")
    public DoctorLocation getLocation(@PathVariable Long id) {
        return service.getLocationById(id);
    }

    @GetMapping("/city/{city}")
    public List<DoctorLocation> getLocationsByCity(@PathVariable String city) {
        return service.getLocationsByCity(city);
    }

    @PutMapping("/{id}")
    public DoctorLocation updateLocation(@PathVariable Long id, @RequestBody DoctorLocation location) {
        return service.updateLocation(id, location);
    }

    @DeleteMapping("/{id}")
    public void deleteLocation(@PathVariable Long id) {
        service.deleteLocation(id);
    }
}
