package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemDTO(
        @NotNull(message = "Cart item ID is required!")
        UUID cartItemId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        @NotNull(message = "Availability Date ID is required!")
        UUID availabilityDateId,

        @NotNull(message = "Quantity is required!")
        @Positive(message = "Quantity must be a positive number!")
        Integer quantity,

        @NotNull(message = "Price is required!")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero!")
        BigDecimal price
) {
}
