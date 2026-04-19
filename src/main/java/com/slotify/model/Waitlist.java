package com.slotify.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private int position;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}