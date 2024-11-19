package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityDateDTO(
        UUID availabilityId,

        @NotNull(message = "Available date is required!")
        LocalDate dateAvailable,

        @NotNull(message = "Remaining seats are required!")
        @PositiveOrZero(message = "Remaining seats must be zero or a positive number!")
        Integer remainingSeats,

        @NotNull(message = "Booking status is required!")
        Boolean isBooked,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId
) {
}
