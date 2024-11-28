package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        UUID cityId,

        @NotNull(message = "Country ID is required!")
        UUID countryId,

        @NotNull(message = "Start date is required!")
        LocalDateTime startDate,

        @NotNull(message = "End date is required!")
        @Future(message = "End date must be in the future!")
        LocalDateTime endDate,

        @NotNull(message = "Markers are required!")
        List<String> markers
) {
}
