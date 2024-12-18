package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingDTO(
        @NotNull(message = "Booking ID is required!")
        UUID bookingId,

        @NotNull(message = "Title is required!")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters!")
        String title,

        @NotNull(message = "Description is required!")
        @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters!")
        String description,

        @NotNull(message = "Booking date is required!")
        LocalDateTime bookingDate,

        @NotNull(message = "Price is required!")
        @Positive(message = "Price must be greater than zero!")
        BigDecimal price,

        @NotNull(message = "Duration is required!")
        @Size(min = 1, max = 50, message = "Duration must be between 1 and 50 characters!")
        String duration,

        @NotNull(message = "Quantity is required!")
        @Positive(message = "Quantity must be greater than zero!")
        Integer quantity,

        @NotNull(message = "Total price is required!")
        @Positive(message = "Total price must be greater than zero!")
        BigDecimal totalPrice
) {
}
