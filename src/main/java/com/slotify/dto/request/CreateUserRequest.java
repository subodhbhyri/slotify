package com.slotify.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority cannot exceed 10")
    private int priority;
}