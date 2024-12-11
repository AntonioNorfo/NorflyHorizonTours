package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartDTO(
        @NotNull(message = "Cart ID is required!")
        UUID cartId,

        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Date added to cart is required!")
        LocalDateTime dateAddedCart,

        @NotNull(message = "Items list cannot be null!")
        @Size(min = 1, message = "Cart must contain at least one item!")
        List<CartItemDTO> items,

        @NotNull(message = "Total amount is required!")
        @Positive(message = "Total amount must be a positive number!")
        BigDecimal totalAmount
) {
}

