package com.slotify.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequest {

    @NotBlank(message = "Event name is required")
    private String name;

    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;
}