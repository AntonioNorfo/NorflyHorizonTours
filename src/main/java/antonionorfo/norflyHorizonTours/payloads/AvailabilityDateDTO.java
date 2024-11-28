package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvailabilityDateDTO(
        UUID availabilityId,

        @NotNull(message = "The available date cannot be null.")
        LocalDateTime dateAvailable,

        @NotNull(message = "The number of remaining seats is required.")
        @PositiveOrZero(message = "The number of remaining seats must be zero or a positive value.")
        Integer remainingSeats,

        @NotNull(message = "Booking status must be specified (true or false).")
        Boolean isBooked,

        @NotNull(message = "The excursion ID is required to associate the availability.")
        UUID excursionId
) {
}