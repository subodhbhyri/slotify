package com.slotify.dto.response;

import com.slotify.model.enums.EventStatus;
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
public class EventResponse {
    private Long id;
    private String name;
    private int totalSeats;
    private int availableSeats;
    private LocalDateTime eventDate;
    private EventStatus status;
    private LocalDateTime createdAt;
}