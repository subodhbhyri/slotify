package com.slotify.service;
import java.util.Optional;
import com.slotify.dto.request.ReserveSeatRequest;
import com.slotify.dto.response.SeatResponse;
import com.slotify.exception.SlotifyException;
import com.slotify.model.Event;
import com.slotify.model.Seat;
import com.slotify.model.User;
import com.slotify.model.enums.SeatStatus;
import com.slotify.repository.EventRepository;
import com.slotify.repository.SeatRepository;
import com.slotify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.slotify.kafka.SeatReservedEvent;
import com.slotify.kafka.SeatCancelledEvent;
import com.slotify.kafka.producer.SlotifyEventProducer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SlotifyEventProducer eventProducer;
    private final WaitlistService waitlistService;
    @Transactional
    public SeatResponse reserveSeat(ReserveSeatRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new SlotifyException("Event not found with id: " + request.getEventId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new SlotifyException("User not found with id: " + request.getUserId()));

        Seat seat = seatRepository
                .findByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SlotifyException("No available seats for event: " + event.getName()));

        seat.setStatus(SeatStatus.RESERVED);
        seat.setUser(user);
        seat.setReservedAt(LocalDateTime.now());

        event.setAvailableSeats(event.getAvailableSeats() - 1);

        eventRepository.save(event);
        Seat saved = seatRepository.save(seat);

        log.info("User {} reserved seat {} for event {}", user.getId(), seat.getSeatNumber(), event.getName());
        eventProducer.publishSeatReserved(SeatReservedEvent.builder()
                .seatId(saved.getId())
                .eventId(event.getId())
                .userId(user.getId())
                .seatNumber(saved.getSeatNumber())
                .eventName(event.getName())
                .reservedAt(saved.getReservedAt())
                .build());
        return mapToResponse(saved);
    }

    @Transactional
    public SeatResponse cancelReservation(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SlotifyException("Seat not found with id: " + seatId));

        if (!seat.getUser().getId().equals(userId)) {
            throw new SlotifyException("Seat does not belong to user: " + userId);
        }

        Event event = seat.getEvent();

        // Check waitlist first — assign seat to next user if someone is waiting
        Optional<Long> nextUserId = waitlistService.popNextFromWaitlist(event.getId());

        if (nextUserId.isPresent()) {
            User nextUser = userRepository.findById(nextUserId.get())
                    .orElseThrow(() -> new SlotifyException("Waitlisted user not found"));

            seat.setUser(nextUser);
            seat.setReservedAt(LocalDateTime.now());
            seat.setStatus(SeatStatus.RESERVED);

            Seat saved = seatRepository.save(seat);
            log.info("Seat {} reassigned from user {} to waitlisted user {}",
                    seat.getSeatNumber(), userId, nextUserId.get());

            eventProducer.publishSeatReserved(SeatReservedEvent.builder()
                    .seatId(saved.getId())
                    .eventId(event.getId())
                    .userId(nextUser.getId())
                    .seatNumber(saved.getSeatNumber())
                    .eventName(event.getName())
                    .reservedAt(saved.getReservedAt())
                    .build());

            return mapToResponse(saved);
        }

        // No one on waitlist — free the seat
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setUser(null);
        seat.setReservedAt(null);
        event.setAvailableSeats(event.getAvailableSeats() + 1);

        eventRepository.save(event);
        Seat saved = seatRepository.save(seat);

        eventProducer.publishSeatCancelled(SeatCancelledEvent.builder()
                .seatId(saved.getId())
                .eventId(event.getId())
                .userId(userId)
                .seatNumber(saved.getSeatNumber())
                .eventName(event.getName())
                .cancelledAt(LocalDateTime.now())
                .build());

        log.info("User {} cancelled seat {} for event {}", userId, seat.getSeatNumber(), event.getName());
        return mapToResponse(saved);
    }

    public List<SeatResponse> getSeatsByEvent(Long eventId) {
        return seatRepository.findByEventIdAndStatus(eventId, SeatStatus.RESERVED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SeatResponse mapToResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .status(seat.getStatus())
                .eventId(seat.getEvent().getId())
                .userId(seat.getUser() != null ? seat.getUser().getId() : null)
                .reservedAt(seat.getReservedAt())
                .build();
    }
}