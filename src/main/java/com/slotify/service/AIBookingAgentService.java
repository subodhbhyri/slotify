package com.slotify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slotify.dto.response.ApiResponse;
import com.slotify.dto.response.EventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIBookingAgentService {

    private final WebClient anthropicWebClient;
    private final EventService eventService;
    private final ReservationService reservationService;
    private final WaitlistService waitlistService;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api.model}")
    private String model;

    @Value("${anthropic.api.max-tokens}")
    private int maxTokens;

    public String processBookingRequest(Long userId, String userMessage) {
        List<EventResponse> activeEvents = eventService.getAllActiveEvents();

        String systemPrompt = buildSystemPrompt(activeEvents);

        String claudeResponse = callClaude(systemPrompt, userMessage);
        log.info("Claude response for user {}: {}", userId, claudeResponse);

        return executeAction(userId, claudeResponse, userMessage);
    }

    private String buildSystemPrompt(List<EventResponse> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are Slotify's AI booking assistant. ");
        sb.append("Help users reserve seats, join waitlists, and get event information.\n\n");
        sb.append("Available events:\n");

        for (EventResponse event : events) {
            sb.append(String.format("- ID: %d, Name: %s, Available Seats: %d, Date: %s\n",
                    event.getId(), event.getName(),
                    event.getAvailableSeats(), event.getEventDate()));
        }

        sb.append("\nRespond ONLY with a JSON object in one of these formats:\n");
        sb.append("1. Reserve seat: {\"action\":\"RESERVE\",\"eventId\":1}\n");
        sb.append("2. Join waitlist: {\"action\":\"WAITLIST\",\"eventId\":1}\n");
        sb.append("3. Check events: {\"action\":\"LIST_EVENTS\"}\n");
        sb.append("4. Unknown intent: {\"action\":\"UNKNOWN\",\"message\":\"clarification message\"}\n");
        sb.append("\nAlways respond with valid JSON only. No extra text.");

        return sb.toString();
    }

    private String callClaude(String systemPrompt, String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", userMessage)
                )
        );

        try {
            String response = anthropicWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            return root.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Failed to call Claude API: {}", e.getMessage());
            return "{\"action\":\"UNKNOWN\",\"message\":\"I'm having trouble processing your request right now.\"}";
        }
    }

    private String executeAction(Long userId, String claudeResponse, String originalMessage) {
        log.info("Executing action for Claude response: {}", claudeResponse);
        try {
            JsonNode action = objectMapper.readTree(claudeResponse);
            String actionType = action.path("action").asText();

            switch (actionType) {
                case "RESERVE": {
                    Long eventId = action.path("eventId").asLong();
                    try {
                        var seat = reservationService.reserveSeat(
                                new com.slotify.dto.request.ReserveSeatRequest(userId, eventId));
                        return String.format(
                                "Done! I've reserved seat %d for you. Enjoy the event!",
                                seat.getSeatNumber());
                    } catch (Exception e) {
                        return "Sorry, I couldn't reserve a seat: " + e.getMessage()
                                + ". Would you like to join the waitlist instead?";
                    }
                }

                case "WAITLIST": {
                    Long eventId = action.path("eventId").asLong();
                    waitlistService.joinWaitlist(eventId, userId);
                    int position = waitlistService.getWaitlistSize(eventId);
                    return String.format(
                            "You've been added to the waitlist at position %d. "
                                    + "I'll automatically assign you a seat when one becomes available.",
                            position);
                }

                case "LIST_EVENTS": {
                    List<EventResponse> events = eventService.getAllActiveEvents();
                    StringBuilder sb = new StringBuilder("Here are the available events:\n");
                    for (EventResponse event : events) {
                        sb.append(String.format("• %s — %d seats available on %s\n",
                                event.getName(), event.getAvailableSeats(),
                                event.getEventDate().toLocalDate()));
                    }
                    return sb.toString();
                }

                default: {
                    String message = action.path("message").asText();
                    return message.isEmpty()
                            ? "I didn't quite understand that. Try saying 'book me a seat for the football game'."
                            : message;
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute action: {}", e.getMessage());
            return "I had trouble processing your request. Please try again.";
        }
    }
}