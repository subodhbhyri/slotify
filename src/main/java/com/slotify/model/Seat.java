package com.slotify.model;
import com.slotify.model.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime reservedAt;
}