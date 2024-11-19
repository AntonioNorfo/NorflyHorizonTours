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
public class AvailabilityDate {
    @Id
    @GeneratedValue
    private UUID availabilityId;

    private LocalDate dateAvailable;
    private Integer remainingSeats;
    private Boolean isBooked;

    @ManyToOne
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;
}
