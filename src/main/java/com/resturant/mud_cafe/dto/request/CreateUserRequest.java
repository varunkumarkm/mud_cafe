package com.resturant.mud_cafe.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private String role; // OWNER, MANAGER, WAITER
}