package antonionorfo.norflyHorizonTours.entities;

import antonionorfo.norflyHorizonTours.enums.PaymentMethod;
import antonionorfo.norflyHorizonTours.enums.PaymentStatus;
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
public class Payment {
    @Id
    @GeneratedValue
    private UUID paymentId;

    private BigDecimal amountPayment;
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod methodPayment;

    @Enumerated(EnumType.STRING)
    private PaymentStatus statusPayment;

    private String transactionReference;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

}