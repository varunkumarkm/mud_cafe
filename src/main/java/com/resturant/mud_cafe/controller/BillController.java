package com.resturant.mud_cafe.controller;

import com.resturant.mud_cafe.dto.request.*;
import com.resturant.mud_cafe.dto.response.BillResponse;
import com.resturant.mud_cafe.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@RequestBody CreateBillRequest request) {
        return ResponseEntity.ok(billService.createBill(request));
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<BillResponse> getBillByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(billService.getBillByTable(tableId));
    }

    @PostMapping("/{billId}/items")
    public ResponseEntity<BillResponse> addItem(
            @PathVariable Long billId,
            @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(billService.addItem(billId, request));
    }

    @DeleteMapping("/{billId}/items/{itemId}")
    public ResponseEntity<BillResponse> removeItem(
            @PathVariable Long billId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(billService.removeItem(billId, itemId));
    }

    @PatchMapping("/{billId}/pay")
    public ResponseEntity<BillResponse> markAsPaid(
            @PathVariable Long billId,
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(billService.markAsPaid(billId, request));
    }

    @PatchMapping("/{billId}/discount")
    public ResponseEntity<BillResponse> applyDiscount(
            @PathVariable Long billId,
            @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(billService.applyDiscount(billId, request));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<BillResponse>> getRecentPaidBills() {
        return ResponseEntity.ok(billService.getRecentPaidBills());
    }
}