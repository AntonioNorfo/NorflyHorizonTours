package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CountryDTO(
        UUID countryId,

        @NotEmpty(message = "Country name is required!")
        @Size(min = 2, max = 100, message = "Country name must be between 2 and 100 characters!")
        String name,

        String code,

        String region
) {
}
