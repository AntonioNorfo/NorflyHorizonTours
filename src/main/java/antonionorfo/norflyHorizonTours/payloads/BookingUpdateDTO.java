package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BookingUpdateDTO(
        @Positive(message = "Quantity must be greater than zero!")
        Integer quantity,

        BigDecimal totalPrice
) {
}
