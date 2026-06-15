package com.health.mediconnectx.controller;

import com.health.mediconnectx.entity.Medicine;
import com.health.mediconnectx.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin("*")
public class MedicineController {

    @Autowired
    private MedicineService service;

    @PostMapping("/create")
    public Medicine createMedicine(@RequestBody Medicine medicine) {
        return service.createMedicine(medicine);
    }

    @GetMapping("/{id}")
    public Medicine getMedicine(@PathVariable Long id) {
        return service.getMedicineById(id);
    }

    @GetMapping("/all")
    public List<Medicine> getAllMedicines() {
        return service.getAllMedicines();
    }

    @GetMapping("/search/{name}")
    public List<Medicine> searchByName(@PathVariable String name) {
        return service.searchMedicineByName(name);
    }

    @GetMapping("/type/{type}")
    public List<Medicine> getMedicinesByType(@PathVariable String type) {
        return service.getMedicinesByType(type);
    }

    @GetMapping("/manufacturer/{manufacturer}")
    public List<Medicine> getMedicinesByManufacturer(@PathVariable String manufacturer) {
        return service.getMedicinesByManufacturer(manufacturer);
    }

    @PutMapping("/{id}")
    public Medicine updateMedicine(@PathVariable Long id, @RequestBody Medicine medicine) {
        return service.updateMedicine(id, medicine);
    }

    @DeleteMapping("/{id}")
    public void deleteMedicine(@PathVariable Long id) {
        service.deleteMedicine(id);
    }
}
