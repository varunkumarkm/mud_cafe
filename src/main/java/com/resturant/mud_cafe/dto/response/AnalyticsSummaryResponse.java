package com.resturant.mud_cafe.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AnalyticsSummaryResponse {
    private BigDecimal totalRevenue;
    private Long totalBills;
    private BigDecimal averageBillValue;
    private Long totalTables;
    private Long occupiedTables;
}