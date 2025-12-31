package com.pharmacy.catalog_service.controller;

import com.pharmacy.catalog_service.dto.*;
import com.pharmacy.catalog_service.entity.Medicine;
import com.pharmacy.catalog_service.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final CatalogService service;

    @GetMapping("/{id}")
    public Medicine get(@PathVariable Long id) { return service.getMedicine(id); }

    @GetMapping("/code/{code}")
    public Medicine getByCode(@PathVariable String code) { return service.getByCode(code); }

    @GetMapping("/barcode/{barcode}")
    public Medicine getByBarcode(@PathVariable String barcode) { return service.getByBarcode(barcode); }

    @GetMapping("/search")
    public List<Medicine> search(@RequestParam String q) { return service.search(q); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Medicine create(@RequestBody CreateMedicineRequest req) { return service.createMedicine(req); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Medicine update(@PathVariable Long id, @RequestBody UpdateMedicineRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.updateMedicine(id, req, userId);
    }

    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Medicine updatePrice(@PathVariable Long id, @RequestBody UpdatePriceRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.updatePrice(id, req.newPrice(), userId);
    }
}
