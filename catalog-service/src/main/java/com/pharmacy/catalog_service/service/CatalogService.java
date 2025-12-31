package com.pharmacy.catalog_service.service;

import com.pharmacy.catalog_service.dto.*;
import com.pharmacy.catalog_service.entity.*;
import com.pharmacy.catalog_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepo categoryRepo;
    private final MedicineRepo medicineRepo;
    private final MedicinePriceHistoryRepo priceHistoryRepo;

    // ===== Category =====
    public List<Category> listCategories() {
        return categoryRepo.findAll();
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest req) {
        Category c = new Category();
        c.setCode(req.code());
        c.setName(req.name());
        c.setDescription(req.description());
        return categoryRepo.save(c);
    }

    // ===== Medicine =====
    public Medicine getMedicine(Long id) {
        return medicineRepo.findById(id).orElseThrow();
    }

    public Medicine getByCode(String code) {
        return medicineRepo.findByCode(code).orElseThrow();
    }

    public Medicine getByBarcode(String barcode) {
        return medicineRepo.findByBarcode(barcode).orElseThrow();
    }

    public List<Medicine> search(String q) {
        return medicineRepo.search(q == null ? "" : q.trim());
    }

    @Transactional
    public Medicine createMedicine(CreateMedicineRequest req) {
        Medicine m = new Medicine();
        m.setCode(req.code());
        m.setName(req.name());
        m.setGenericName(req.genericName());
        m.setUnit(req.unit());
        m.setRx(req.isRx() != null && req.isRx());
        m.setManufacturer(req.manufacturer());
        m.setCategoryId(req.categoryId());
        m.setDefaultSupplierId(req.defaultSupplierId());
        m.setSalePrice(req.salePrice() == null ? BigDecimal.ZERO : req.salePrice());
        m.setBarcode(req.barcode());
        m.setImageUrl(req.imageUrl());
        m.setDescription(req.description());
        m.setUsageInstructions(req.usageInstructions());
        m.setSideEffects(req.sideEffects());
        m.setStatus(req.status() == null ? "ACTIVE" : req.status());

        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return medicineRepo.save(m);
    }

    @Transactional
    public Medicine updateMedicine(Long id, UpdateMedicineRequest req, Long userIdForPriceHistory) {
        Medicine m = medicineRepo.findById(id).orElseThrow();
        if (req.code() != null) m.setCode(req.code());
        if (req.name() != null) m.setName(req.name());
        if (req.genericName() != null) m.setGenericName(req.genericName());
        if (req.unit() != null) m.setUnit(req.unit());
        if (req.isRx() != null) m.setRx(req.isRx());
        if (req.manufacturer() != null) m.setManufacturer(req.manufacturer());
        if (req.categoryId() != null) m.setCategoryId(req.categoryId());
        if (req.defaultSupplierId() != null) m.setDefaultSupplierId(req.defaultSupplierId());
        if (req.barcode() != null) m.setBarcode(req.barcode());
        if (req.imageUrl() != null) m.setImageUrl(req.imageUrl());
        if (req.description() != null) m.setDescription(req.description());
        if (req.usageInstructions() != null) m.setUsageInstructions(req.usageInstructions());
        if (req.sideEffects() != null) m.setSideEffects(req.sideEffects());
        if (req.status() != null) m.setStatus(req.status());

        // nếu update salePrice thì ghi lịch sử
        if (req.salePrice() != null && m.getSalePrice() != null && req.salePrice().compareTo(m.getSalePrice()) != 0) {
            insertPriceHistory(m.getId(), m.getSalePrice(), req.salePrice(), userIdForPriceHistory);
            m.setSalePrice(req.salePrice());
        } else if (req.salePrice() != null && m.getSalePrice() == null) {
            insertPriceHistory(m.getId(), BigDecimal.ZERO, req.salePrice(), userIdForPriceHistory);
            m.setSalePrice(req.salePrice());
        }

        m.setUpdatedAt(LocalDateTime.now());
        return medicineRepo.save(m);
    }

    @Transactional
    public Medicine updatePrice(Long id, BigDecimal newPrice, Long userId) {
        Medicine m = medicineRepo.findById(id).orElseThrow();
        BigDecimal old = m.getSalePrice() == null ? BigDecimal.ZERO : m.getSalePrice();

        if (newPrice == null) throw new RuntimeException("newPrice is required");
        if (newPrice.compareTo(old) != 0) {
            insertPriceHistory(m.getId(), old, newPrice, userId);
            m.setSalePrice(newPrice);
            m.setUpdatedAt(LocalDateTime.now());
            return medicineRepo.save(m);
        }
        return m;
    }

    private void insertPriceHistory(Long medicineId, BigDecimal oldPrice, BigDecimal newPrice, Long userId) {
        MedicinePriceHistory h = new MedicinePriceHistory();
        h.setMedicineId(medicineId);
        h.setOldPrice(oldPrice);
        h.setNewPrice(newPrice);
        h.setChangedBy(userId);
        h.setChangedAt(LocalDateTime.now());
        priceHistoryRepo.save(h);
    }
}
