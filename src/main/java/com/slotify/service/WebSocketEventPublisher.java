package com.slotify.service;

import com.slotify.dto.response.SeatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishSeatReserved(Long eventId, SeatResponse seat) {
        String destination = "/topic/events/" + eventId + "/seats";
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "SEAT_RESERVED",
                "seatId", seat.getId(),
                "seatNumber", seat.getSeatNumber(),
                "userId", seat.getUserId(),
                "eventId", eventId
        ));
        log.info("WebSocket published SEAT_RESERVED for seat {} on event {}",
                seat.getSeatNumber(), eventId);
    }

    public void publishSeatAvailable(Long eventId, SeatResponse seat) {
        String destination = "/topic/events/" + eventId + "/seats";
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "SEAT_AVAILABLE",
                "seatId", seat.getId(),
                "seatNumber", seat.getSeatNumber(),
                "eventId", eventId
        ));
        log.info("WebSocket published SEAT_AVAILABLE for seat {} on event {}",
                seat.getSeatNumber(), eventId);
    }

    public void publishWaitlistUpdate(Long eventId, int waitlistSize) {
        String destination = "/topic/events/" + eventId + "/waitlist";
        messagingTemplate.convertAndSend(destination, Map.of(
                "type", "WAITLIST_UPDATE",
                "eventId", eventId,
                "waitlistSize", waitlistSize
        ));
        log.info("WebSocket published WAITLIST_UPDATE for event {}, size {}",
                eventId, waitlistSize);
    }
}