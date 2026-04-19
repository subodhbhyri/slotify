package com.slotify.repository;

import com.slotify.model.Seat;
import com.slotify.model.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);
    Optional<Seat> findByEventIdAndSeatNumber(Long eventId, int seatNumber);
    int countByEventIdAndStatus(Long eventId, SeatStatus status);
}