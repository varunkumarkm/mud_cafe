package com.resturant.mud_cafe.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DiscountRequest {
    private String type;
    private BigDecimal value;
}