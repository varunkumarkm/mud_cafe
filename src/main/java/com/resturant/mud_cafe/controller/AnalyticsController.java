package com.resturant.mud_cafe.controller;

import com.resturant.mud_cafe.dto.response.AnalyticsSummaryResponse;
import com.resturant.mud_cafe.dto.response.RevenueDataResponse;
import com.resturant.mud_cafe.dto.response.TopItemResponse;
import com.resturant.mud_cafe.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getDailySummary() {
        return ResponseEntity.ok(analyticsService.getDailySummary());
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueDataResponse>> getRevenue(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(analyticsService.getRevenueByRange(from, to));
    }

    @GetMapping("/top-items")
    public ResponseEntity<List<TopItemResponse>> getTopItems(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(analyticsService.getTopItems(from, to));
    }
}