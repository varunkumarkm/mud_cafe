package com.resturant.mud_cafe.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TableResponse {
    private Long id;
    private String name;
    private String floor;
    private String section;
    private Integer capacity;
    private String status;
    private LocalDateTime createdAt;
}