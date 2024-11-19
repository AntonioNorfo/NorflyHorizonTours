package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record FavoriteDTO(
        UUID favoriteId,

        @NotNull(message = "User ID is required!")
        UUID userId,

        UUID cityId,
        UUID excursionId,

        LocalDate addedFavoriteDate
) {
}
