package com.pharmacy.inventory_service.controller;

import com.pharmacy.inventory_service.dto.*;
import com.pharmacy.inventory_service.entity.StockTransaction;
import com.pharmacy.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    /* =======================
       INBOUND (from PURCHASE)
       ======================= */
    @PostMapping("/inbound")
    public InboundResponse inbound(@RequestBody InboundRequest req) {
        return service.inbound(req);
    }

    /* =======================
       POS FLOW (CASHIER)
       ======================= */
    @PostMapping("/reserve")
    public ReserveResponse reserve(@RequestBody ReserveRequest req, Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        return service.reserve(req, userId);
    }

    @PostMapping("/commit")
    public void commit(@RequestBody RefRequest req, Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        service.commit(req.refType(), req.refId(), userId);
    }

    @PostMapping("/release")
    public void release(@RequestBody RefRequest req, Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        service.release(req.refType(), req.refId(), userId);
    }

    /* =======================
       VIEW STOCK (for FE)
       ======================= */
    @GetMapping("/lots")
    public List<StockLotDto> lots(@RequestParam(required = false) Long medicineId,
                                  @RequestParam(required = false) LocalDate expBefore,
                                  @RequestParam(defaultValue = "false") boolean onlyAvailable){
        return service.listLots(medicineId, expBefore, onlyAvailable);
    }

    @GetMapping("/lots/{lotId}")
    public StockLotDto lot(@PathVariable Long lotId){
        return service.getLot(lotId);
    }

    @GetMapping("/summary")
    public List<StockSummaryDto> summary(@RequestParam(required = false) Long medicineId){
        return service.summary(medicineId);
    }

    /* =======================
       ALERTS
       ======================= */
    @GetMapping("/alerts/low-stock")
    public List<LowStockAlertDto> lowStock(){
        return service.lowStockAlerts();
    }

    @GetMapping("/alerts/expiry")
    public List<ExpiryAlertDto> expiry(@RequestParam(required = false) LocalDate before){
        return service.expiryAlerts(before);
    }

    /* =======================
       TRANSACTIONS HISTORY
       ======================= */
    @GetMapping("/transactions")
    public List<StockTransaction> transactions(@RequestParam(required = false) String refType,
                                               @RequestParam(required = false) String refId,
                                               @RequestParam(required = false) Long medicineId,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) LocalDateTime dateFrom,
                                               @RequestParam(required = false) LocalDateTime dateTo){
        return service.searchTx(refType, refId, medicineId, type, dateFrom, dateTo);
    }

    /* =======================
       ADMIN ACTIONS
       ======================= */
    @PostMapping("/adjust")
    public void adjust(@RequestBody AdjustRequest req, Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        service.adjust(req, userId);
    }

    @PostMapping("/mark-damaged")
    public void markDamaged(@RequestBody LotActionRequest req, Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        service.markDamaged(req, userId);
    }

    @PostMapping("/mark-expired")
    public void markExpired(@RequestBody LotActionRequest req, Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        service.markExpired(req, userId);
    }
}
