package com.slotify.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.slotify.kafka.EventCreatedEvent;
import com.slotify.kafka.SeatCancelledEvent;
import com.slotify.kafka.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.slotify.kafka.WaitlistJoinedEvent;
import static com.slotify.config.KafkaConfig.WAITLIST_JOINED_TOPIC;
import static com.slotify.config.KafkaConfig.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotifyEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public void publishSeatReserved(SeatReservedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(SEAT_RESERVED_TOPIC,
                    String.valueOf(event.getEventId()), payload);
            log.info("Published SeatReservedEvent for seat {} on event {}",
                    event.getSeatNumber(), event.getEventName());
        } catch (Exception e) {
            log.error("Failed to publish SeatReservedEvent: {}", e.getMessage());
        }
    }

    public void publishSeatCancelled(SeatCancelledEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(SEAT_CANCELLED_TOPIC,
                    String.valueOf(event.getEventId()), payload);
            log.info("Published SeatCancelledEvent for seat {} on event {}",
                    event.getSeatNumber(), event.getEventName());
        } catch (Exception e) {
            log.error("Failed to publish SeatCancelledEvent: {}", e.getMessage());
        }
    }

    public void publishEventCreated(EventCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(EVENT_CREATED_TOPIC,
                    String.valueOf(event.getEventId()), payload);
            log.info("Published EventCreatedEvent for event {}",
                    event.getEventName());
        } catch (Exception e) {
            log.error("Failed to publish EventCreatedEvent: {}", e.getMessage());
        }
    }

    public void publishWaitlistJoined(WaitlistJoinedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(WAITLIST_JOINED_TOPIC,
                    String.valueOf(event.getEventId()), payload);
            log.info("Published WaitlistJoinedEvent for user {} on event {}",
                    event.getUserId(), event.getEventName());
        } catch (Exception e) {
            log.error("Failed to publish WaitlistJoinedEvent: {}", e.getMessage());
        }
    }
}