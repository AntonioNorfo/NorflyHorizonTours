package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "excursion_id", nullable = false)
    private Excursion excursion;

    private Integer quantity;

    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "availability_date_id", nullable = false)
    private AvailabilityDate availabilityDate;


    public BigDecimal calculatePrice() {
        return excursion.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
