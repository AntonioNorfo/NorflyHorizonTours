package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ExcursionDTO(
        UUID excursionId,

        @NotEmpty(message = "Title is required!")
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters!")
        String title,

        @NotEmpty(message = "Description is required!")
        String descriptionExcursion,

        @NotNull(message = "Price is required!")
        @Positive(message = "Price must be a positive number!")
        BigDecimal price,

        @NotEmpty(message = "Duration is required!")
        String duration,

        @NotNull(message = "Difficulty level is required!")
        DifficultyLevel difficultyLevel,

        @NotEmpty(message = "Inclusions are required!")
        String inclusions,

        @NotEmpty(message = "Rules are required!")
        String rules,

        String notRecommended,

        @NotNull(message = "Max participants is required!")
        @Positive(message = "Max participants must be a positive number!")
        Integer maxParticipants,

        @NotNull(message = "City ID is required!")
        UUID cityId
) {
}
