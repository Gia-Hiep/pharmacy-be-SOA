package com.pharmacy.purchase_service.controller;

import com.pharmacy.purchase_service.dto.CreateSupplierRequest;
import com.pharmacy.purchase_service.entity.Supplier;
import com.pharmacy.purchase_service.repository.SupplierRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/purchase/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierRepo supplierRepo;

    @PostMapping
    public Supplier create(@RequestBody CreateSupplierRequest req){
        Supplier s = new Supplier();
        s.setCode(req.code());
        s.setName(req.name());
        s.setContactPerson(req.contactPerson());
        s.setPhone(req.phone());
        s.setEmail(req.email());
        s.setAddress(req.address());
        s.setNotes(req.notes());
        s.setCreatedAt(LocalDateTime.now());
        return supplierRepo.save(s);
    }

    @GetMapping
    public List<Supplier> list(){
        return supplierRepo.findAll();
    }
}
