package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record BookingDTO(
        UUID bookingId,

        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        @NotNull(message = "Booking date is required!")
        LocalDate bookingDate,

        LocalDate startDate,
        LocalDate endDate,

        @NotEmpty(message = "Booking status is required!")
        String statusOfBooking
) {
}
