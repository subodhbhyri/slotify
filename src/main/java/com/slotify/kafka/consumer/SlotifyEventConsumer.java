package com.slotify.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.slotify.kafka.EventCreatedEvent;
import com.slotify.kafka.SeatCancelledEvent;
import com.slotify.kafka.SeatReservedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.slotify.kafka.WaitlistJoinedEvent;
import static com.slotify.config.KafkaConfig.WAITLIST_JOINED_TOPIC;
import static com.slotify.config.KafkaConfig.*;

@Component
@Slf4j
public class SlotifyEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @KafkaListener(topics = SEAT_RESERVED_TOPIC, groupId = "slotify-group")
    public void consumeSeatReserved(String payload) {
        try {
            SeatReservedEvent event = objectMapper.readValue(payload, SeatReservedEvent.class);
            log.info("KAFKA [seat-reserved] User {} reserved seat {} for event '{}'",
                    event.getUserId(), event.getSeatNumber(), event.getEventName());
        } catch (Exception e) {
            log.error("Failed to consume SeatReservedEvent: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = SEAT_CANCELLED_TOPIC, groupId = "slotify-group")
    public void consumeSeatCancelled(String payload) {
        try {
            SeatCancelledEvent event = objectMapper.readValue(payload, SeatCancelledEvent.class);
            log.info("KAFKA [seat-cancelled] User {} cancelled seat {} for event '{}'",
                    event.getUserId(), event.getSeatNumber(), event.getEventName());
        } catch (Exception e) {
            log.error("Failed to consume SeatCancelledEvent: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = EVENT_CREATED_TOPIC, groupId = "slotify-group")
    public void consumeEventCreated(String payload) {
        try {
            EventCreatedEvent event = objectMapper.readValue(payload, EventCreatedEvent.class);
            log.info("KAFKA [event-created] New event '{}' created with {} seats",
                    event.getEventName(), event.getTotalSeats());
        } catch (Exception e) {
            log.error("Failed to consume EventCreatedEvent: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = WAITLIST_JOINED_TOPIC, groupId = "slotify-group")
    public void consumeWaitlistJoined(String payload) {
        try {
            WaitlistJoinedEvent event = objectMapper.readValue(payload, WaitlistJoinedEvent.class);
            log.info("KAFKA [waitlist-joined] User {} joined waitlist for event '{}' at position {}",
                    event.getUserId(), event.getEventName(), event.getPosition());
        } catch (Exception e) {
            log.error("Failed to consume WaitlistJoinedEvent: {}", e.getMessage());
        }
    }
}