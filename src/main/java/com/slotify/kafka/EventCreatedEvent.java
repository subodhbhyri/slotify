package com.slotify.kafka;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreatedEvent {
    private Long eventId;
    private String eventName;
    private int totalSeats;
    private LocalDateTime eventDate;
    private LocalDateTime createdAt;
}