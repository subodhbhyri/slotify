package com.slotify.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String SEAT_RESERVED_TOPIC = "seat-reserved";
    public static final String SEAT_CANCELLED_TOPIC = "seat-cancelled";
    public static final String EVENT_CREATED_TOPIC = "event-created";
    public static final String WAITLIST_JOINED_TOPIC = "waitlist-joined";

    @Bean
    public NewTopic seatReservedTopic() {
        return TopicBuilder.name(SEAT_RESERVED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic seatCancelledTopic() {
        return TopicBuilder.name(SEAT_CANCELLED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic eventCreatedTopic() {
        return TopicBuilder.name(EVENT_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic waitlistJoinedTopic() {
        return TopicBuilder.name(WAITLIST_JOINED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}