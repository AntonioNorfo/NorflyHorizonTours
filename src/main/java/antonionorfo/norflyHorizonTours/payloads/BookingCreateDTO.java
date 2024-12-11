package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingCreateDTO(
        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        @NotNull(message = "Booking date and time is required!")
        LocalDateTime bookingDateTime,

        @NotNull(message = "End date and time is required!")
        LocalDateTime endDateTime,

        int numSeats
) {
}
