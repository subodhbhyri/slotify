package com.slotify.repository;

import com.slotify.model.Event;
import com.slotify.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);
    boolean existsByNameAndStatus(String name, EventStatus status);
}