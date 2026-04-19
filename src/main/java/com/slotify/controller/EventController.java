package com.slotify.controller;

import com.slotify.dto.request.CreateEventRequest;
import com.slotify.dto.response.ApiResponse;
import com.slotify.dto.response.EventResponse;
import com.slotify.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        EventResponse event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", event));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", event));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllActiveEvents() {
        List<EventResponse> events = eventService.getAllActiveEvents();
        return ResponseEntity.ok(ApiResponse.success("Active events retrieved successfully", events));
    }
}