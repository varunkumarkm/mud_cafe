package com.resturant.mud_cafe.service;

import com.resturant.mud_cafe.dto.request.AddItemRequest;
import com.resturant.mud_cafe.dto.request.CreateBillRequest;
import com.resturant.mud_cafe.dto.request.DiscountRequest;
import com.resturant.mud_cafe.dto.request.PaymentRequest;
import com.resturant.mud_cafe.dto.response.BillItemResponse;
import com.resturant.mud_cafe.dto.response.BillResponse;
import com.resturant.mud_cafe.entity.*;
import com.resturant.mud_cafe.enums.BillStatus;
import com.resturant.mud_cafe.enums.PaymentMethod;
import com.resturant.mud_cafe.enums.TableStatus;
import com.resturant.mud_cafe.exception.BadRequestException;
import com.resturant.mud_cafe.exception.ResourceNotFoundException;
import com.resturant.mud_cafe.repository.*;
import com.resturant.mud_cafe.websocket.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");

    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + request.getTableId()));

        // Check if there's already an open bill
        billRepository.findByTableAndStatus(table, BillStatus.OPEN)
                .ifPresent(b -> { throw new BadRequestException("Table " + b.getTable().getName() + " already has an open bill"); });

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User staff = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

        Bill bill = Bill.builder()
                .table(table)
                .staff(staff)
                .items(new ArrayList<>())
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .status(BillStatus.OPEN)
                .build();

        Bill saved = billRepository.save(bill);

        // Mark table as occupied
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);
        notificationPublisher.publishTableStatusUpdate(table.getId(), "OCCUPIED");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BillResponse getBillByTable(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        Bill bill = billRepository.findByTableAndStatus(table, BillStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No open bill for this table"));
        return toResponse(bill);
    }

    @Transactional
    public BillResponse addItem(Long billId, AddItemRequest request) {
        Bill bill = getOpenBill(billId);

        BigDecimal lineTotal = request.getUnitPrice()
                .multiply(new BigDecimal(request.getQuantity()));

        BillItem item = BillItem.builder()
                .bill(bill)
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .lineTotal(lineTotal)
                .build();

        billItemRepository.save(item);
        return recalculate(bill);
    }

    @Transactional
    public BillResponse removeItem(Long billId, Long itemId) {
        Bill bill = getOpenBill(billId);
        billItemRepository.deleteById(itemId);
        return recalculate(bill);
    }

    @Transactional
    public BillResponse applyDiscount(Long billId, DiscountRequest request) {
        Bill bill = getOpenBill(billId);

        BigDecimal subtotal = bill.getItems().stream()
                .map(BillItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount;
        if ("PERCENTAGE".equals(request.getType())) {
            discount = subtotal.multiply(request.getValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = request.getValue();
        }

        bill.setDiscount(discount);
        return recalculate(bill);
    }

    @Transactional
    public BillResponse markAsPaid(Long billId, PaymentRequest request) {
        Bill bill = getOpenBill(billId);

        bill.setStatus(BillStatus.PAID);
        bill.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        bill.setPaidAt(LocalDateTime.now());
        Bill saved = billRepository.save(bill);

        // Update table status to PAID
        RestaurantTable table = bill.getTable();
        table.setStatus(TableStatus.PAID);
        tableRepository.save(table);

        // Broadcast table status update
        notificationPublisher.publishTableStatusUpdate(table.getId(), "PAID");

        // Build and send payment notification
        Map<String, Object> payload = new HashMap<>();
        payload.put("billId", saved.getId());
        payload.put("tableNumber", table.getName());
        payload.put("total", saved.getTotal());
        payload.put("paymentMethod", request.getPaymentMethod());
        payload.put("waiter", bill.getStaff().getName());
        payload.put("timestamp", saved.getPaidAt().toString());
        notificationPublisher.publishPaymentNotification(payload);

        userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("OWNER")
                        || u.getRole().name().equals("MANAGER"))
                .forEach(recipient -> {
                    Notification notification = Notification.builder()
                            .bill(saved)
                            .message("Table " + table.getName() + " paid ₹"
                                    + saved.getTotal() + " via "
                                    + request.getPaymentMethod())
                            .type("PAYMENT")
                            .recipient(recipient)
                            .isRead(false)
                            .build();
                    notificationRepository.save(notification);
                });

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getRecentPaidBills() {
        return billRepository.findRecentPaidBills(
                        org.springframework.data.domain.PageRequest.of(0, 20))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Bill getOpenBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
        if (bill.getStatus() != BillStatus.OPEN) {
            throw new BadRequestException("Bill " + billId + " is already closed");
        }
        return bill;
    }

    private BillResponse recalculate(Bill bill) {
        // Re-fetch items from DB to get latest
        List<BillItem> items = billItemRepository.findByBillId(bill.getId());

        BigDecimal subtotal = items.stream()
                .map(BillItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = bill.getDiscount() != null
                ? bill.getDiscount() : BigDecimal.ZERO;

        BigDecimal total = subtotal.add(tax).subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);

        bill.setSubtotal(subtotal);
        bill.setTax(tax);
        bill.setTotal(total);

        return toResponse(billRepository.save(bill));
    }

    private BillResponse toResponse(Bill bill) {
        List<BillItem> items = billItemRepository.findByBillId(bill.getId());

        List<BillItemResponse> itemResponses = items.stream()
                .map(i -> BillItemResponse.builder()
                        .id(i.getId())
                        .itemName(i.getItemName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .lineTotal(i.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        return BillResponse.builder()
                .id(bill.getId())
                .tableId(bill.getTable().getId())
                .tableName(bill.getTable().getName())
                .staffName(bill.getStaff().getName())
                .items(itemResponses)
                .subtotal(bill.getSubtotal())
                .tax(bill.getTax())
                .discount(bill.getDiscount())
                .total(bill.getTotal())
                .paymentMethod(bill.getPaymentMethod() != null
                        ? bill.getPaymentMethod().name() : null)
                .status(bill.getStatus().name())
                .createdAt(bill.getCreatedAt())
                .paidAt(bill.getPaidAt())
                .build();
    }
}