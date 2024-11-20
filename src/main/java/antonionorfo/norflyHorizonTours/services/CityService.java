package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO.MarkerInfo;
import antonionorfo.norflyHorizonTours.repositories.CityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);
    private final RestTemplate restTemplate;
    private final CityRepository cityRepository;

    @Value("${geonames.username}")
    private String geonamesUsername;

    // Fetch all countries using RestCountries API
    public List<Map<String, String>> fetchAllCountries() {
        String url = "https://restcountries.com/v3.1/all";
        logger.info("Fetching all countries from RestCountries API");

        try {
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);

            return response.stream()
                    .map(country -> Map.of(
                            "name", (String) ((Map<String, Object>) country.get("name")).get("common"),
                            "code", (String) country.get("cca2"),
                            "region", (String) country.get("region")
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching countries: {}", e.getMessage());
            throw new RuntimeException("Error while calling RestCountries API", e);
        }
    }

    // Fetch countries by region
    public List<Map<String, String>> fetchCountriesByRegion(String region) {
        logger.info("Fetching countries by region: {}", region);
        return fetchAllCountries().stream()
                .filter(country -> region.equalsIgnoreCase(country.get("region")))
                .collect(Collectors.toList());
    }

    // Fetch detailed information for a specific country
    public CountryDetailsDTO fetchCountryDetails(String countryCode) {
        String url = String.format("https://restcountries.com/v3.1/alpha/%s", countryCode);
        logger.info("Fetching details for country: {}", countryCode);

        try {
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);

            if (response == null || response.isEmpty()) {
                logger.error("No data found for country code: {}", countryCode);
                throw new IllegalStateException("Invalid response from RestCountries API");
            }

            Map<String, Object> country = response.get(0);

            // Create the DTO
            return CountryDetailsDTO.builder()
                    .name(((Map<String, String>) country.get("name")).get("common"))
                    .officialName(((Map<String, String>) country.get("name")).get("official"))
                    .capital(country.containsKey("capital") ? ((List<String>) country.get("capital")).get(0) : null)
                    .population(toLong(country.get("population")))
                    .area(toDouble(country.get("area")))
                    .region((String) country.get("region"))
                    .subregion((String) country.get("subregion"))
                    .languages(country.containsKey("languages")
                            ? new ArrayList<>(((Map<String, String>) country.get("languages")).values())
                            : List.of())
                    .currency(extractCurrency(country))
                    .flag((String) country.get("flag"))
                    .maps((Map<String, String>) country.get("maps"))
                    .markerInfo(createMarkerInfo((List<?>) country.get("latlng")))
                    .build();
        } catch (Exception e) {
            logger.error("Error fetching details for country: {}", e.getMessage());
            throw new RuntimeException("Error while fetching country details", e);
        }
    }

    // Extract currency from the country data
    private String extractCurrency(Map<String, Object> country) {
        if (!country.containsKey("currencies")) {
            return null;
        }

        Map<String, Map<String, String>> currencies = (Map<String, Map<String, String>>) country.get("currencies");
        Map.Entry<String, Map<String, String>> entry = currencies.entrySet().iterator().next();
        return entry.getValue().get("name") + " (" + entry.getValue().get("symbol") + ")";
    }

    // Create MarkerInfo
    private MarkerInfo createMarkerInfo(List<?> latlng) {
        if (latlng == null || latlng.size() < 2) {
            throw new IllegalArgumentException("Invalid latitude and longitude data");
        }
        return new MarkerInfo(toDouble(latlng.get(0)), toDouble(latlng.get(1)));
    }

    // Convert to Long
    private Long toLong(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return null;
    }

    // Convert to Double
    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    // Fetch cities by country using GeoNames API
    public List<String> fetchCitiesByCountry(String countryCode) {
        String url = String.format(
                "http://api.geonames.org/searchJSON?country=%s&featureClass=P&maxRows=100&username=%s",
                countryCode, geonamesUsername
        );

        logger.info("Fetching cities for country code: {}", countryCode);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("geonames")) {
                logger.error("No data found for country code: {}", countryCode);
                throw new IllegalStateException("Invalid response from GeoNames API");
            }

            List<Map<String, Object>> geonames = (List<Map<String, Object>>) response.get("geonames");
            return geonames.stream()
                    .map(geoname -> (String) geoname.get("name"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error while fetching cities: {}", e.getMessage());
            throw new RuntimeException("Error while calling GeoNames API", e);
        }
    }

    // Save a city into the database
    public City saveCity(String name, String country, String coordinates) {
        logger.info("Saving city: {} in country: {}", name, country);

        Optional<City> existingCity = cityRepository.findByNameAndCountry(name, country);
        if (existingCity.isPresent()) {
            logger.info("City already exists: {}", existingCity.get());
            return existingCity.get();
        }

        City city = new City();
        city.setName(name);
        city.setCountry(country);
        city.setCoordinates(coordinates);

        return cityRepository.save(city);
    }

    // Retrieve a list of cities saved for a specific country
    public List<City> getCitiesByCountry(String country) {
        logger.info("Retrieving saved cities for country: {}", country);
        return cityRepository.findByCountry(country);
    }
}
