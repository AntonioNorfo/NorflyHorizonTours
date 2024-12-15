package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import jakarta.validation.constraints.*;

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
        BigDecimal price,

        @NotEmpty(message = "Title is required!")
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters!")
        String title,

        @NotEmpty(message = "Description is required!")
        String descriptionExcursion,

        @NotEmpty(message = "Duration is required!")
        String duration,

        @NotNull(message = "Difficulty level is required!")
        DifficultyLevel difficultyLevel

) {
}
