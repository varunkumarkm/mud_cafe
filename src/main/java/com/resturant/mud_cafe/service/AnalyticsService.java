package com.resturant.mud_cafe.service;

import com.resturant.mud_cafe.dto.response.AnalyticsSummaryResponse;
import com.resturant.mud_cafe.dto.response.RevenueDataResponse;
import com.resturant.mud_cafe.dto.response.TopItemResponse;
import com.resturant.mud_cafe.enums.TableStatus;
import com.resturant.mud_cafe.repository.BillItemRepository;
import com.resturant.mud_cafe.repository.BillRepository;
import com.resturant.mud_cafe.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final TableRepository tableRepository;

    public AnalyticsSummaryResponse getDailySummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        var bills = billRepository.findByPaidAtBetween(startOfDay, endOfDay);

        BigDecimal totalRevenue = bills.stream()
                .map(b -> b.getTotal() != null ? b.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalBills = bills.size();

        BigDecimal avgBill = totalBills > 0
                ? totalRevenue.divide(new BigDecimal(totalBills), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long totalTables = tableRepository.count();
        long occupiedTables = tableRepository.findByStatus(TableStatus.OCCUPIED).size();

        return AnalyticsSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalBills(totalBills)
                .averageBillValue(avgBill)
                .totalTables(totalTables)
                .occupiedTables(occupiedTables)
                .build();
    }

    public List<RevenueDataResponse> getRevenueByRange(String from, String to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDate.parse(from, formatter).atStartOfDay();
        LocalDateTime end = LocalDate.parse(to, formatter).plusDays(1).atStartOfDay();

        var bills = billRepository.findByPaidAtBetween(start, end);

        // Group by date
        Map<String, List<BigDecimal>> grouped = new LinkedHashMap<>();
        for (var bill : bills) {
            String date = bill.getPaidAt().toLocalDate().toString();
            grouped.computeIfAbsent(date, k -> new ArrayList<>())
                    .add(bill.getTotal() != null ? bill.getTotal() : BigDecimal.ZERO);
        }

        return grouped.entrySet().stream()
                .map(entry -> RevenueDataResponse.builder()
                        .date(entry.getKey())
                        .revenue(entry.getValue().stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .billCount((long) entry.getValue().size())
                        .build())
                .collect(Collectors.toList());
    }

    public List<TopItemResponse> getTopItems(String from, String to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDate.parse(from, formatter).atStartOfDay();
        LocalDateTime end = LocalDate.parse(to, formatter).plusDays(1).atStartOfDay();

        var bills = billRepository.findByPaidAtBetween(start, end);

        Map<String, long[]> itemMap = new HashMap<>();
        // [0] = totalQuantity, [1] = totalRevenue (as cents)

        for (var bill : bills) {
            var items = billItemRepository.findByBillId(bill.getId());
            for (var item : items) {
                itemMap.computeIfAbsent(item.getItemName(), k -> new long[]{0, 0});
                itemMap.get(item.getItemName())[0] += item.getQuantity();
                itemMap.get(item.getItemName())[1] +=
                        item.getLineTotal().multiply(new BigDecimal("100")).longValue();
            }
        }

        return itemMap.entrySet().stream()
                .map(e -> TopItemResponse.builder()
                        .itemName(e.getKey())
                        .totalQuantity(e.getValue()[0])
                        .totalRevenue(new BigDecimal(e.getValue()[1])
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP))
                        .build())
                .sorted((a, b) -> b.getTotalQuantity().compareTo(a.getTotalQuantity()))
                .collect(Collectors.toList());
    }
}