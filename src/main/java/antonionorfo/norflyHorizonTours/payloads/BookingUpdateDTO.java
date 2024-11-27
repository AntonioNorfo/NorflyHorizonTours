package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.Future;

import java.time.LocalDate;

public record BookingUpdateDTO(
        @Future(message = "Start date must be in the future")
        LocalDate startDate,

        @Future(message = "End date must be in the future")
        LocalDate endDate
) {
}
