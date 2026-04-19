package com.slotify.model;
import com.slotify.model.enums.EventStatus;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int availableSeats;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Seat> seats;

    @CreationTimestamp
    private LocalDateTime createdAt;
}