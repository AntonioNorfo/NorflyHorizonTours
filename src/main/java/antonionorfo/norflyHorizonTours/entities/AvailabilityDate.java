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
        if (dateAvailable.isBefore(excursion.getStartDate()) || dateAvailable.isAfter(excursion.getEndDate())) {
            throw new IllegalArgumentException("La disponibilità deve essere compresa tra la data di inizio e fine dell'escursione.");
        }
    }
}
