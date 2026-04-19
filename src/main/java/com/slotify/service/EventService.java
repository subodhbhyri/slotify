package com.slotify.service;

import com.slotify.dto.request.CreateEventRequest;
import com.slotify.dto.response.EventResponse;
import com.slotify.exception.SlotifyException;
import com.slotify.model.Event;
import com.slotify.model.Seat;
import com.slotify.model.enums.EventStatus;
import com.slotify.model.enums.SeatStatus;
import com.slotify.repository.EventRepository;
import com.slotify.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.slotify.kafka.EventCreatedEvent;
import com.slotify.kafka.producer.SlotifyEventProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final SlotifyEventProducer eventProducer;
    @Transactional
    @CacheEvict(value = "activeEvents", allEntries = true)
    public EventResponse createEvent(CreateEventRequest request) {
        if (eventRepository.existsByNameAndStatus(request.getName(), EventStatus.ACTIVE)) {
            throw new SlotifyException("Active event with name " + request.getName() + " already exists");
        }

        Event event = Event.builder()
                .name(request.getName())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .eventDate(request.getEventDate())
                .status(EventStatus.ACTIVE)
                .build();

        Event saved = eventRepository.save(event);

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= request.getTotalSeats(); i++) {
            seats.add(Seat.builder()
                    .seatNumber(i)
                    .status(SeatStatus.AVAILABLE)
                    .event(saved)
                    .build());
        }
        seatRepository.saveAll(seats);

        log.info("Created event '{}' with {} seats", saved.getName(), saved.getTotalSeats());
        eventProducer.publishEventCreated(EventCreatedEvent.builder()
                .eventId(saved.getId())
                .eventName(saved.getName())
                .totalSeats(saved.getTotalSeats())
                .eventDate(saved.getEventDate())
                .createdAt(saved.getCreatedAt())
                .build());
        return mapToResponse(saved);
    }

    @Cacheable(value = "events", key = "#id")
    public EventResponse getEventById(Long id) {
        log.info("Fetching event {} from database", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new SlotifyException("Event not found with id: " + id));
        return mapToResponse(event);
    }

    @Cacheable(value = "activeEvents")
    public List<EventResponse> getAllActiveEvents() {
        log.info("Fetching active events from database");
        return eventRepository.findByStatus(EventStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"events", "activeEvents"}, allEntries = true)
    public void evictEventCache(Long id) {
        log.info("Evicting cache for event {}", id);
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .eventDate(event.getEventDate())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .build();
    }
}