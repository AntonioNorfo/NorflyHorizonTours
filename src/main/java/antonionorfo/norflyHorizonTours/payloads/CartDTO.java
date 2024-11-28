package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartDTO(
        UUID cartId,

        UUID userId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        LocalDateTime dateAddedCart,

        @NotNull(message = "Quantity is required!")
        Integer quantity
) {
}

