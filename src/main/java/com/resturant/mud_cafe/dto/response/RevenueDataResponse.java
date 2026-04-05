package com.resturant.mud_cafe.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RevenueDataResponse {
    private String date;
    private BigDecimal revenue;
    private Long billCount;
}