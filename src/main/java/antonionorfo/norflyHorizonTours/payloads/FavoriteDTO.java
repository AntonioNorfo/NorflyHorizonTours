package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record FavoriteDTO(
        UUID favoriteId,

        @NotNull(message = "User ID is required!")
        UUID userId,

        UUID cityId,
        UUID excursionId,

        LocalDate addedFavoriteDate,

        @NotEmpty(message = "Title is required!")
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters!")
        String title,

        @NotEmpty(message = "Description is required!")
        String descriptionExcursion

) {
}
