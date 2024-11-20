package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record CountryDetailsDTO(
        @NotEmpty(message = "Country name is required!")
        String name,

        @NotEmpty(message = "Official country name is required!")
        String officialName,

        String capital,

        @NotNull(message = "Population is required!")
        long population,

        @NotNull(message = "Area is required!")
        double area,

        @NotEmpty(message = "Region is required!")
        String region,

        String subregion,

        List<String> languages,

        String currency,

        String flag,

        Map<String, String> maps,

        @NotNull(message = "Marker information is required!")
        MarkerInfo markerInfo
) {
    @Builder
    public static record MarkerInfo(
            @NotNull(message = "Latitude is required!")
            double latitude,

            @NotNull(message = "Longitude is required!")
            double longitude
    ) {}
}
