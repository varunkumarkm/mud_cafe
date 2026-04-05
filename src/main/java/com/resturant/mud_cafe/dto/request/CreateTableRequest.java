package com.resturant.mud_cafe.dto.request;

import lombok.Data;

@Data
public class CreateTableRequest {
    private String name;
    private String floor;
    private String section;
    private Integer capacity;
}