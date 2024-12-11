package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record BookingUpdateDTO(
        @Future(message = "Start date must be in the future")
        LocalDateTime startDate,

        @Future(message = "End date must be in the future")
        LocalDateTime endDate,

        @Min(value = 1, message = "At least 1 seat must be booked!")
        Integer numSeats
) {
}
