package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.PaymentMethod;
import antonionorfo.norflyHorizonTours.enums.PaymentStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDTO(
        UUID paymentId,

        @Positive(message = "Payment amount must be positive!")
        BigDecimal amountPayment,

        @NotNull(message = "Payment date is required!")
        LocalDateTime paymentDate,

        @NotNull(message = "Payment method is required!")
        PaymentMethod methodPayment,

        @NotNull(message = "Payment status is required!")
        PaymentStatus statusPayment,

        @NotEmpty(message = "Transaction reference is required!")
        String transactionReference,

        @NotNull(message = "Cart ID is required!")
        UUID cartId,

        @NotNull(message = "User ID is required!")
        UUID userId
) {
}
