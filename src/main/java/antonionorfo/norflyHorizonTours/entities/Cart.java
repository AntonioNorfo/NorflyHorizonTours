package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue
    private UUID cartId;

    private LocalDateTime dateAddedCart;

    private BigDecimal totalAmount = BigDecimal.ZERO;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;

    @ManyToOne
    @JoinColumn(name = "availability_date_id")
    private AvailabilityDate availabilityDate;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", totalItems=" + (items != null ? items.size() : 0) +
                ", totalAmount=" + totalAmount +
                ", userId=" + (user != null ? user.getUserId() : "null") +
                '}';
    }
}

