package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CityDTO(
        UUID cityId,

        @NotEmpty(message = "City name is required!")
        @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters!")
        String name,

        @NotNull(message = "Country ID is required!")
        UUID countryId,

        @NotEmpty(message = "City description is required!")
        String description,

        @NotEmpty(message = "Coordinates are required!")
        String coordinates,

        String code
) {
}
