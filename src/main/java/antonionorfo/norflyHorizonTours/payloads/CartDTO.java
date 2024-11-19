package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartDTO(
        UUID cartId,

        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        @NotNull(message = "Date added to cart is required!")
        LocalDateTime dateAddedCart
) {
}
