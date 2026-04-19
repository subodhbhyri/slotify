package com.slotify.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int priority;

    @OneToMany(mappedBy = "user")
    private List<Seat> reservations;

    @CreationTimestamp
    private LocalDateTime createdAt;
}