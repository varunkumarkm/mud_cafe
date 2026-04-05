package com.resturant.mud_cafe.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddItemRequest {
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
}