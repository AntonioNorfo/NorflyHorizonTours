package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.PaymentMethod;
import antonionorfo.norflyHorizonTours.enums.PaymentStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDTO(
        UUID paymentId,

        @NotNull(message = "Payment amount is required!")
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

        @NotNull(message = "Booking ID is required!")
        UUID bookingId
) {
}
