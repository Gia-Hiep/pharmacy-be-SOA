package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.dto.*;
import com.pharmacy.sales_service.entity.Invoice;
import com.pharmacy.sales_service.entity.InvoiceItem;
import com.pharmacy.sales_service.entity.Payment;
import com.pharmacy.sales_service.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService service;

    /* =======================
       CREATE / ADD / CHECKOUT / PAY
       ======================= */

    @PostMapping("/invoices")
    public Invoice create(@RequestBody CreateInvoiceRequest req,
                          Authentication auth,
                          @RequestHeader("Authorization") String authHeader){
        Long userId = (Long) auth.getPrincipal();
        String token = authHeader.substring(7);

        return service.createInvoice(req.code(), userId, token, req.customerPhone(), req.customerName());
    }

    @PostMapping("/invoices/{invoiceId}/items")
    public InvoiceItem addItem(@PathVariable Long invoiceId,
                               @RequestBody AddItemRequest req,
                               @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        return service.addItem(invoiceId, req, token);
    }

    @PostMapping("/invoices/{invoiceId}/checkout")
    public CheckoutResponse checkout(@PathVariable Long invoiceId,
                                     @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        return service.checkout(invoiceId, token);
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    public void pay(@PathVariable Long invoiceId,
                    @RequestBody PayRequest req,
                    @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        service.pay(invoiceId, req, token);
    }

    /* =======================
       READ FOR FE (GET invoices/items/payments)
       ======================= */

    // list invoices with filters
    @GetMapping("/invoices")
    public List<Invoice> listInvoices(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String paymentStatus,
                                      @RequestParam(required = false) Long cashierId,
                                      @RequestParam(required = false) LocalDateTime dateFrom,
                                      @RequestParam(required = false) LocalDateTime dateTo){
        return service.listInvoices(status, paymentStatus, cashierId, dateFrom, dateTo);
    }

    @GetMapping("/invoices/{invoiceId}")
    public Invoice getInvoice(@PathVariable Long invoiceId){
        return service.getInvoice(invoiceId);
    }

    @GetMapping("/invoices/{invoiceId}/items")
    public List<InvoiceItem> getItems(@PathVariable Long invoiceId){
        return service.getItems(invoiceId);
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    public List<Payment> getPayments(@PathVariable Long invoiceId){
        return service.getPayments(invoiceId);
    }

    /* =======================
       CANCEL + RELEASE (if reserved)
       ======================= */

    @PostMapping("/invoices/{invoiceId}/cancel")
    public Invoice cancel(@PathVariable Long invoiceId,
                          @RequestBody(required = false) CancelInvoiceRequest req,
                          Authentication auth,
                          @RequestHeader("Authorization") String authHeader){
        Long userId = (Long) auth.getPrincipal();
        String token = authHeader.substring(7);
        String reason = (req == null) ? null : req.reason();
        return service.cancelInvoice(invoiceId, token, userId, reason);
    }

    /* =======================
       ATTACH PRESCRIPTION
       ======================= */

    @PostMapping("/invoices/{invoiceId}/attach-prescription")
    public Invoice attachPrescription(@PathVariable Long invoiceId,
                                      @RequestBody AttachPrescriptionRequest req,
                                      Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        return service.attachPrescription(invoiceId, req.prescriptionId(), userId);
    }
}
