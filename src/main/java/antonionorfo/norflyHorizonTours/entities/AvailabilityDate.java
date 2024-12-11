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
public class AvailabilityDate {
    @Id
    @GeneratedValue
    private UUID availabilityId;

    private LocalDateTime dateAvailable;

    private Integer remainingSeats;

    private Boolean isBooked;

    @ManyToOne
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;

    @PrePersist
    @PreUpdate
    private void validateDateAvailability() {
        if (remainingSeats < 0) {
            throw new IllegalArgumentException("Il numero di posti rimanenti non può essere negativo.");
        }

        if (dateAvailable == null) {
            throw new IllegalArgumentException("La data di disponibilità non può essere null.");
        }
    }
}
