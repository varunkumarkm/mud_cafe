package com.resturant.mud_cafe.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopItemResponse {
    private String itemName;
    private Long totalQuantity;
    private java.math.BigDecimal totalRevenue;
}