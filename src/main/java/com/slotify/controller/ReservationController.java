package com.slotify.controller;

import com.slotify.dto.request.ReserveSeatRequest;
import com.slotify.dto.response.ApiResponse;
import com.slotify.dto.response.SeatResponse;
import com.slotify.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.slotify.service.WaitlistService;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitlistService waitlistService;

    @PostMapping("/waitlist")
    public ResponseEntity<ApiResponse<Void>> joinWaitlist(
            @RequestParam Long eventId,
            @RequestParam Long userId) {
        waitlistService.joinWaitlist(eventId, userId);
        return ResponseEntity.ok(ApiResponse.success("Joined waitlist successfully", null));
    }

    @DeleteMapping("/waitlist")
    public ResponseEntity<ApiResponse<Void>> leaveWaitlist(
            @RequestParam Long eventId,
            @RequestParam Long userId) {
        waitlistService.leaveWaitlist(eventId, userId);
        return ResponseEntity.ok(ApiResponse.success("Left waitlist successfully", null));
    }

    @GetMapping("/waitlist/{eventId}/size")
    public ResponseEntity<ApiResponse<Integer>> getWaitlistSize(@PathVariable Long eventId) {
        int size = waitlistService.getWaitlistSize(eventId);
        return ResponseEntity.ok(ApiResponse.success("Waitlist size retrieved", size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeatResponse>> reserveSeat(
            @Valid @RequestBody ReserveSeatRequest request) {
        SeatResponse seat = reservationService.reserveSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seat reserved successfully", seat));
    }

    @DeleteMapping("/{seatId}/users/{userId}")
    public ResponseEntity<ApiResponse<SeatResponse>> cancelReservation(
            @PathVariable Long seatId,
            @PathVariable Long userId) {
        SeatResponse seat = reservationService.cancelReservation(seatId, userId);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully", seat));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByEvent(
            @PathVariable Long eventId) {
        List<SeatResponse> seats = reservationService.getSeatsByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success("Reservations retrieved successfully", seats));
    }
}