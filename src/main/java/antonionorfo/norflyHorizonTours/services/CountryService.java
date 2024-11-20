package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Country;
import antonionorfo.norflyHorizonTours.payloads.CountryDTO;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO;
import antonionorfo.norflyHorizonTours.repositories.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryService {

    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;

    @Value("${geonames.username}")
    private String geonamesUsername;

    // Fetch all countries from DB
    public List<CountryDTO> getAllCountriesFromDB() {
        logger.info("Fetching all countries from the database.");
        return countryRepository.findAll().stream()
                .map(country -> new CountryDTO(
                        country.getId(),
                        country.getName(),
                        country.getCode(),
                        country.getRegion()
                ))
                .collect(Collectors.toList());
    }

    // Fetch all countries from GeoNames API
    public List<CountryDTO> getAllCountriesFromGeoNames() {
        String url = String.format("http://api.geonames.org/countryInfoJSON?username=%s", geonamesUsername);
        logger.info("Fetching all countries from GeoNames API.");

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("geonames")) {
                return ((List<Map<String, Object>>) response.get("geonames")).stream()
                        .map(data -> new CountryDTO(
                                null,
                                (String) data.get("countryName"),
                                (String) data.get("countryCode"),
                                (String) data.get("continent")
                        ))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error fetching countries from GeoNames API.", e);
        }

        logger.warn("No countries found in GeoNames API response.");
        return List.of();
    }

    // Fetch country details from DB by name, code, or UUID
    public CountryDTO getCountryDetailsFromDB(String countryIdentifier) {
        logger.info("Fetching country details from DB for identifier: {}", countryIdentifier);

        Country country = null;

        if (isUUID(countryIdentifier)) {
            country = countryRepository.findById(UUID.fromString(countryIdentifier))
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with ID: " + countryIdentifier));
        } else if (countryIdentifier.length() == 2) {
            country = countryRepository.findByCode(countryIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with code: " + countryIdentifier));
        } else {
            country = countryRepository.findByName(countryIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with name: " + countryIdentifier));
        }

        return new CountryDTO(country.getId(), country.getName(), country.getCode(), country.getRegion());
    }

    // Fetch country details from GeoNames API
    public CountryDetailsDTO getCountryDetailsFromGeoNames(String countryCode) {
        String url = String.format("https://restcountries.com/v3.1/alpha/%s", countryCode);
        logger.info("Fetching details for country from GeoNames: {}", countryCode);

        try {
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            if (response == null || response.isEmpty()) {
                logger.error("No data found for country code: {}", countryCode);
                throw new IllegalStateException("Invalid response from RestCountries API");
            }

            Map<String, Object> country = response.get(0);
            return CountryDetailsDTO.builder()
                    .name(((Map<String, String>) country.get("name")).get("common"))
                    .officialName(((Map<String, String>) country.get("name")).get("official"))
                    .capital(country.containsKey("capital") ? ((List<String>) country.get("capital")).get(0) : null)
                    .population(country.get("population") != null ? ((Number) country.get("population")).longValue() : 0L)
                    .area(country.get("area") != null ? ((Number) country.get("area")).doubleValue() : 0.0)
                    .region((String) country.get("region"))
                    .subregion((String) country.get("subregion"))
                    .languages(country.containsKey("languages")
                            ? new ArrayList<>(((Map<String, String>) country.get("languages")).values())
                            : null)
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

    // Fetch countries by region from DB
    public List<CountryDTO> getCountriesByRegionFromDB(String region) {
        logger.info("Fetching countries by region from DB: {}", region);
        return countryRepository.findByRegion(region).stream()
                .map(country -> new CountryDTO(
                        country.getId(),
                        country.getName(),
                        country.getCode(),
                        country.getRegion()
                ))
                .collect(Collectors.toList());
    }

    // Fetch countries by region from GeoNames API
    public List<CountryDTO> getCountriesByRegionFromGeoNames(String region) {
        logger.info("Fetching countries by region from GeoNames API: {}", region);
        return getAllCountriesFromGeoNames().stream()
                .filter(country -> region.equalsIgnoreCase(country.region()))
                .collect(Collectors.toList());
    }

    // Populate DB with countries from GeoNames API
    public void populateCountries() {
        String url = String.format("http://api.geonames.org/countryInfoJSON?username=%s", geonamesUsername);
        logger.info("Populating database with countries from GeoNames API.");

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("geonames")) {
                List<Map<String, Object>> countries = (List<Map<String, Object>>) response.get("geonames");

                countries.forEach(countryData -> {
                    String name = (String) countryData.get("countryName");
                    String code = (String) countryData.get("countryCode");
                    String continent = (String) countryData.get("continent");

                    if (!countryRepository.existsByName(name)) {
                        Country country = new Country();
                        country.setName(name);
                        country.setCode(code);
                        country.setRegion(continent);
                        countryRepository.save(country);
                        logger.info("Saved country: {} ({})", name, code);
                    }
                });
            } else {
                logger.warn("No countries found in GeoNames API response.");
            }
        } catch (Exception e) {
            logger.error("Error populating countries from GeoNames API.", e);
        }
    }

    // Helper method to extract currency
    private String extractCurrency(Map<String, Object> country) {
        if (!country.containsKey("currencies")) {
            return null;
        }

        Map<String, Map<String, String>> currencies = (Map<String, Map<String, String>>) country.get("currencies");
        if (currencies.isEmpty()) {
            return null;
        }

        Map.Entry<String, Map<String, String>> entry = currencies.entrySet().iterator().next();
        Map<String, String> currencyInfo = entry.getValue();

        return currencyInfo.get("name") + " (" + currencyInfo.get("symbol") + ")";
    }

    // Helper method to create MarkerInfo
    private CountryDetailsDTO.MarkerInfo createMarkerInfo(List<?> latlng) {
        if (latlng == null || latlng.size() < 2) {
            return null;
        }
        return CountryDetailsDTO.MarkerInfo.builder()
                .latitude(((Number) latlng.get(0)).doubleValue())
                .longitude(((Number) latlng.get(1)).doubleValue())
                .build();
    }

    // Helper method to check if a string is a valid UUID
    private boolean isUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
