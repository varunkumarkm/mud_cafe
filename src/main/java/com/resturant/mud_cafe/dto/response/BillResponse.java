package com.resturant.mud_cafe.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BillResponse {
    private Long id;
    private Long tableId;
    private String tableName;
    private String staffName;
    private List<BillItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}