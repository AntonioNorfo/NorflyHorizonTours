package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue
    private UUID notificationId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime dateCreated;
    private Boolean isRead;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
