package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue
    private UUID reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Integer rating;
    private LocalDate reviewDate;

    private boolean blocked = false;
}
