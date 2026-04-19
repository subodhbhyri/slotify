package com.slotify.kafka;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatReservedEvent {
    private Long seatId;
    private Long eventId;
    private Long userId;
    private int seatNumber;
    private String eventName;
    private LocalDateTime reservedAt;
}