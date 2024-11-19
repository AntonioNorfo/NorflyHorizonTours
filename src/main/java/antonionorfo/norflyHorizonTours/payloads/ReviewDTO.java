package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ReviewDTO(
        UUID reviewId,

        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId,

        @NotEmpty(message = "Comment is required!")
        String comment,

        @NotNull(message = "Rating is required!")
        @Min(value = 1, message = "Rating must be at least 1!")
        @Max(value = 5, message = "Rating cannot exceed 5!")
        Integer rating,

        LocalDate reviewDate
) {
}
