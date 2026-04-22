package com.slotify.controller;

import com.slotify.dto.response.ApiResponse;
import com.slotify.service.AIBookingAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIBookingController {

    private final AIBookingAgentService aiBookingAgentService;

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<String>> processBooking(
            @RequestParam Long userId,
            @RequestBody Map<String, String> body) {
        String message = body.get("message");
        String response = aiBookingAgentService.processBookingRequest(userId, message);
        return ResponseEntity.ok(ApiResponse.success("AI response", response));
    }
}