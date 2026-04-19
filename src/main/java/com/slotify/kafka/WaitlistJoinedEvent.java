package com.slotify.kafka;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistJoinedEvent {
    private Long userId;
    private Long eventId;
    private String eventName;
    private int priority;
    private int position;
    private LocalDateTime joinedAt;
}