package com.slotify.service;

import com.slotify.exception.SlotifyException;
import com.slotify.kafka.WaitlistJoinedEvent;
import com.slotify.kafka.producer.SlotifyEventProducer;
import com.slotify.model.Event;
import com.slotify.model.User;
import com.slotify.model.Waitlist;
import com.slotify.repository.EventRepository;
import com.slotify.repository.UserRepository;
import com.slotify.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SlotifyEventProducer eventProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String WAITLIST_KEY = "waitlist:event:";

    @Transactional
    public void joinWaitlist(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new SlotifyException("Event not found: " + eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SlotifyException("User not found: " + userId));

        if (waitlistRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new SlotifyException("User " + userId + " is already on the waitlist for this event");
        }

        int position = waitlistRepository.countByEventId(eventId) + 1;

        Waitlist waitlist = Waitlist.builder()
                .user(user)
                .event(event)
                .priority(user.getPriority())
                .position(position)
                .build();

        waitlistRepository.save(waitlist);

        // Store in Redis sorted set — score is negative priority so highest priority
        // sorts first (Redis sorts ascending by score)
        double score = -user.getPriority();
        redisTemplate.opsForZSet().add(WAITLIST_KEY + eventId,
                String.valueOf(userId), score);

        log.info("User {} joined waitlist for event {} at position {}", userId, eventId, position);

        eventProducer.publishWaitlistJoined(WaitlistJoinedEvent.builder()
                .userId(userId)
                .eventId(eventId)
                .eventName(event.getName())
                .priority(user.getPriority())
                .position(position)
                .joinedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public Optional<Long> popNextFromWaitlist(Long eventId) {
        // Get highest priority user from Redis sorted set
        var result = redisTemplate.opsForZSet()
                .popMin(WAITLIST_KEY + eventId);

        if (result == null) return Optional.empty();

        Long userId = Long.valueOf(result.getValue().toString());
        waitlistRepository.deleteByEventIdAndUserId(eventId, userId);
        log.info("Popped user {} from waitlist for event {}", userId, eventId);
        return Optional.of(userId);
    }

    @Transactional
    public void leaveWaitlist(Long eventId, Long userId) {
        if (!waitlistRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new SlotifyException("User " + userId + " is not on the waitlist for this event");
        }
        waitlistRepository.deleteByEventIdAndUserId(eventId, userId);
        redisTemplate.opsForZSet().remove(WAITLIST_KEY + eventId, String.valueOf(userId));
        log.info("User {} left waitlist for event {}", userId, eventId);
    }

    public int getWaitlistSize(Long eventId) {
        Long size = redisTemplate.opsForZSet().size(WAITLIST_KEY + eventId);
        return size != null ? size.intValue() : 0;
    }
}