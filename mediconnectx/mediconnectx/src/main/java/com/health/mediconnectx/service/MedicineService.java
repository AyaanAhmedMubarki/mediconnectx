package com.health.mediconnectx.service;

import com.health.mediconnectx.entity.Medicine;
import com.health.mediconnectx.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository repository;

    public Medicine createMedicine(Medicine medicine) {
        return repository.save(medicine);
    }

    public Medicine getMedicineById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Medicine> getAllMedicines() {
        return repository.findAll();
    }

    public List<Medicine> searchMedicineByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public List<Medicine> getMedicinesByType(String type) {
        return repository.findByType(type);
    }

    public List<Medicine> getMedicinesByManufacturer(String manufacturer) {
        return repository.findByManufacturer(manufacturer);
    }

    public Medicine updateMedicine(Long id, Medicine medicine) {
        Medicine existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(medicine.getName());
            existing.setGenericName(medicine.getGenericName());
            existing.setManufacturer(medicine.getManufacturer());
            existing.setPrice(medicine.getPrice());
            existing.setDosage(medicine.getDosage());
            existing.setType(medicine.getType());
            existing.setDescription(medicine.getDescription());
            existing.setSideEffects(medicine.getSideEffects());
            existing.setContraindications(medicine.getContraindications());
            existing.setStockQuantity(medicine.getStockQuantity());
            return repository.save(existing);
        }
        return null;
    }

    public void deleteMedicine(Long id) {
        repository.deleteById(id);
    }
}
