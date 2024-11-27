package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record BookingCreateDTO(
        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        @NotNull(message = "Start date is required!")
        LocalDate startDate,

        @NotNull(message = "End date is required!")
        LocalDate endDate
) {
}
