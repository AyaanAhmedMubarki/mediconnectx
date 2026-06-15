package com.health.mediconnectx.service;

import com.health.mediconnectx.entity.DoctorLocation;
import com.health.mediconnectx.repository.DoctorLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorLocationService {

    @Autowired
    private DoctorLocationRepository repository;

    public DoctorLocation createLocation(DoctorLocation location) {
        return repository.save(location);
    }

    public List<DoctorLocation> getLocationsByDoctorId(Long doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public DoctorLocation getLocationById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public DoctorLocation updateLocation(Long id, DoctorLocation location) {
        DoctorLocation existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setClinicName(location.getClinicName());
            existing.setAddress(location.getAddress());
            existing.setCity(location.getCity());
            existing.setState(location.getState());
            existing.setPincode(location.getPincode());
            existing.setPhoneNumber(location.getPhoneNumber());
            return repository.save(existing);
        }
        return null;
    }

    public void deleteLocation(Long id) {
        repository.deleteById(id);
    }

    public List<DoctorLocation> getLocationsByCity(String city) {
        return repository.findByCity(city);
    }
}
