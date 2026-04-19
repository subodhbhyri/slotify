package com.slotify.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveSeatRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Event ID is required")
    private Long eventId;
}