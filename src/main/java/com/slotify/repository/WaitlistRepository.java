package com.slotify.repository;

import com.slotify.model.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByEventIdOrderByPriorityDescJoinedAtAsc(Long eventId);
    Optional<Waitlist> findByEventIdAndUserId(Long eventId, Long userId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    void deleteByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.event.id = :eventId")
    int countByEventId(Long eventId);
}