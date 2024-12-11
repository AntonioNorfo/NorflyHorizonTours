package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingDTO(
        UUID bookingId,

        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        @NotNull(message = "Booking date is required!")
        LocalDateTime bookingDate,

        LocalDateTime startDate,
        LocalDateTime endDate,

        Integer numSeats,

        @NotEmpty(message = "Booking status is required!")
        String statusOfBooking
) {
}
