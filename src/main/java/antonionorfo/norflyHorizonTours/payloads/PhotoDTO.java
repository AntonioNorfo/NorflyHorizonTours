package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record PhotoDTO(
        UUID photoId,

        @NotEmpty(message = "Photo URL is required!")
        String photoOfExcursion,

        @NotNull(message = "Cover photo status is required!")
        Boolean isCoverPhoto,

        @NotNull(message = "Excursion ID is required!")
        UUID excursionId
) {
}
