package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue
    private UUID bookingId;

    private LocalDateTime bookingDate;
    private Integer quantity;
    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;

    @ManyToOne
    @JoinColumn(name = "availability_date_id")
    private AvailabilityDate availabilityDate;

    @Override
    public String toString() {
        return "Booking{" +
                "bookingDate=" + bookingDate +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", user=" + user +
                ", excursion=" + excursion +
                ", availabilityDate=" + availabilityDate +
                '}';
    }
}

