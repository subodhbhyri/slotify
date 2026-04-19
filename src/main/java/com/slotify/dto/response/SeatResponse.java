package com.slotify.dto.response;

import com.slotify.model.enums.SeatStatus;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {
    private Long id;
    private int seatNumber;
    private SeatStatus status;
    private Long eventId;
    private Long userId;
    private LocalDateTime reservedAt;
}