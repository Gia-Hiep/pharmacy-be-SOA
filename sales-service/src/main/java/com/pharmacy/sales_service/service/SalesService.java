package com.pharmacy.sales_service.service;

import com.pharmacy.sales_service.client.CatalogClient;
import com.pharmacy.sales_service.client.CustomerClient;
import com.pharmacy.sales_service.client.InventoryClient;
import com.pharmacy.sales_service.dto.*;
import com.pharmacy.sales_service.entity.*;
import com.pharmacy.sales_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final InvoiceRepo invoiceRepo;
    private final InvoiceItemRepo itemRepo;
    private final InvoiceLotAllocationRepo allocRepo;
    private final PaymentRepo paymentRepo;

    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;
    private final CatalogClient catalogClient;

    /* =======================
       CREATE INVOICE
       ======================= */
    @Transactional
    public Invoice createInvoice(String code, Long cashierId, String bearerToken, String customerPhone, String customerName){
        Long customerId = null;

        if (customerPhone != null && !customerPhone.isBlank()) {
            var c = customerClient.getByPhone(bearerToken, customerPhone);

            if (c == null) {
                String name = (customerName == null || customerName.isBlank()) ? "khach le" : customerName;
                c = customerClient.create(
                        bearerToken,
                        new CustomerClient.CreateCustomerDto(
                                name,
                                customerPhone,
                                null, null, null,
                                "created from sales-service"
                        )
                );
            }
            customerId = c.id();
        }

        Invoice inv = new Invoice();
        inv.setCode(code);
        inv.setCashierId(cashierId);
        inv.setCustomerId(customerId);
        inv.setStatus("DRAFT");
        inv.setPaymentStatus("UNPAID");
        inv.setSubtotal(BigDecimal.ZERO);
        inv.setDiscount(BigDecimal.ZERO);
        inv.setTotal(BigDecimal.ZERO);
        inv.setCreatedAt(LocalDateTime.now());
        inv.setUpdatedAt(LocalDateTime.now());
        return invoiceRepo.save(inv);
    }

    /* =======================
       ADD ITEM (price from catalog)
       ======================= */
    @Transactional
    public InvoiceItem addItem(Long invoiceId, AddItemRequest req, String bearerToken){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (!"DRAFT".equals(inv.getStatus())) throw new RuntimeException("Invoice not DRAFT");
        if (req.qty() <= 0) throw new RuntimeException("qty must be > 0");

        var med = catalogClient.getMedicineById(bearerToken, req.medicineId());
        if (med == null || med.salePrice() == null) throw new RuntimeException("Medicine not found or missing salePrice");

        BigDecimal unitPrice = med.salePrice();
        BigDecimal line = unitPrice.multiply(BigDecimal.valueOf(req.qty()));

        InvoiceItem it = new InvoiceItem();
        it.setInvoiceId(invoiceId);
        it.setMedicineId(req.medicineId());
        it.setQty(req.qty());
        it.setUnitPrice(unitPrice);
        it.setLineTotal(line);
        itemRepo.save(it);

        // update totals
        recalcTotals(inv);

        return it;
    }

    private void recalcTotals(Invoice inv){
        BigDecimal subtotal = itemRepo.findByInvoiceId(inv.getId()).stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        inv.setSubtotal(subtotal);
        inv.setTotal(subtotal.subtract(inv.getDiscount() == null ? BigDecimal.ZERO : inv.getDiscount()));
        inv.setUpdatedAt(LocalDateTime.now());
        invoiceRepo.save(inv);
    }

    /* =======================
       CHECKOUT -> reserve inventory + allocations + status WAIT_PAYMENT
       ======================= */
    @Transactional
    public CheckoutResponse checkout(Long invoiceId, String bearerToken){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if ("WAIT_PAYMENT".equals(inv.getStatus())) {
            throw new RuntimeException("Invoice already checkout, please pay or cancel");
        }
        if (!"DRAFT".equals(inv.getStatus())) {
            throw new RuntimeException("Invoice status=" + inv.getStatus() + " cannot checkout");
        }

        var items = itemRepo.findByInvoiceId(invoiceId);
        if (items.isEmpty()) throw new RuntimeException("No items");

        List<InventoryReserveRequest.Item> reqItems = new ArrayList<>();
        for (var it : items){
            reqItems.add(new InventoryReserveRequest.Item(it.getMedicineId(), it.getQty()));
        }

        InventoryReserveRequest req = new InventoryReserveRequest("INVOICE", inv.getCode(), "FEFO", reqItems);
        InventoryReserveResponse resp = inventoryClient.reserve(bearerToken, req);

        // clear old allocations if re-checkout (optional safe)
        allocRepo.deleteByInvoiceId(invoiceId);

        // naive mapping (same as anh)
        for (var a : resp.reservations()){
            InvoiceItem target = items.stream()
                    .filter(x -> x.getMedicineId().equals(a.medicineId()))
                    .findFirst()
                    .orElseThrow();

            InvoiceLotAllocation al = new InvoiceLotAllocation();
            al.setInvoiceItemId(target.getId());
            al.setLotId(a.lotId());
            al.setQty(a.qty());
            allocRepo.save(al);
        }

        inv.setStatus("WAIT_PAYMENT");
        inv.setUpdatedAt(LocalDateTime.now());
        invoiceRepo.save(inv);

        return new CheckoutResponse(inv.getCode(), resp.reservations());
    }

    /* =======================
       PAY -> commit inventory + payment + status PAID
       ======================= */
    @Transactional
    public void pay(Long invoiceId, PayRequest req, String bearerToken){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        if (!"WAIT_PAYMENT".equals(inv.getStatus())) throw new RuntimeException("Invoice not WAIT_PAYMENT");

        inventoryClient.commit(bearerToken, "INVOICE", inv.getCode());

        Payment p = new Payment();
        p.setInvoiceId(invoiceId);
        p.setAmount(req.amount());
        p.setPaymentMethod(req.paymentMethod());
        p.setTransactionId(req.transactionId());
        p.setStatus("SUCCESS");
        p.setPaidAt(LocalDateTime.now());
        paymentRepo.save(p);

        inv.setPaymentStatus("PAID");
        inv.setStatus("PAID");
        inv.setUpdatedAt(LocalDateTime.now());
        invoiceRepo.save(inv);
    }

    /* =======================
       READ FOR FE
       ======================= */
    @Transactional(readOnly = true)
    public List<Invoice> listInvoices(String status, String paymentStatus, Long cashierId,
                                      LocalDateTime from, LocalDateTime to){
        return invoiceRepo.search(status, paymentStatus, cashierId, from, to);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(Long invoiceId){
        return invoiceRepo.findById(invoiceId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<InvoiceItem> getItems(Long invoiceId){
        return itemRepo.findByInvoiceId(invoiceId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPayments(Long invoiceId){
        return paymentRepo.findByInvoiceId(invoiceId);
    }

    /* =======================
       CANCEL + RELEASE
       ======================= */
    @Transactional
    public Invoice cancelInvoice(Long invoiceId, String bearerToken, Long userId, String reason){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();

        // only allow cancel if not PAID
        if ("PAID".equals(inv.getStatus())) throw new RuntimeException("Cannot cancel a PAID invoice");

        // if already reserved, must release
        if ("WAIT_PAYMENT".equals(inv.getStatus())) {
            inventoryClient.release(bearerToken, "INVOICE", inv.getCode());
        }

        inv.setStatus("CANCELLED");
        inv.setPaymentStatus("UNPAID");
        if (reason != null && !reason.isBlank()){
            inv.setNotes(reason);
        }
        inv.setUpdatedAt(LocalDateTime.now());
        return invoiceRepo.save(inv);
    }

    /* =======================
       ATTACH PRESCRIPTION
       ======================= */
    @Transactional
    public Invoice attachPrescription(Long invoiceId, Long prescriptionId, Long userId){
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();

        // allow only before PAID
        if ("PAID".equals(inv.getStatus())) throw new RuntimeException("Cannot attach prescription to PAID invoice");

        inv.setPrescriptionId(prescriptionId);
        inv.setUpdatedAt(LocalDateTime.now());
        return invoiceRepo.save(inv);
    }
}
