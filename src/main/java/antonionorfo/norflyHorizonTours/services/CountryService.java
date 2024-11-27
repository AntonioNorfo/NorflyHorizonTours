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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryService {

    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;

    @Value("${geonames.username}")
    private String geonamesUsername;

    public List<CountryDTO> getAllCountriesFromDB() {
        logger.info("Fetching all countries from DB.");
        return countryRepository.findAll().stream()
                .map(country -> new CountryDTO(
                        country.getId(),
                        country.getName(),
                        country.getCode(),
                        country.getRegion()
                ))
                .collect(Collectors.toList());
    }

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
            logger.error("Error fetching countries from GeoNames API: {}", e.getMessage());
        }

        logger.warn("No countries found in GeoNames API response.");
        return Collections.emptyList();
    }

    public CountryDTO getCountryDetailsFromDB(String countryIdentifier) {
        logger.info("Fetching country details from DB for identifier: {}", countryIdentifier);
        Country country = findCountryByIdentifier(countryIdentifier);
        return new CountryDTO(country.getId(), country.getName(), country.getCode(), country.getRegion());
    }

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

    public List<CountryDTO> getCountriesByRegionFromDB(String region) {
        logger.info("Fetching countries by region from DB: {}", region);

        List<Country> countries;

        if (region.length() == 2) {
            logger.info("Region code detected: {}", region);
            countries = countryRepository.findByRegion(region);
        } else {
            logger.info("Full region name detected: {}", region);
            countries = countryRepository.findByRegionIgnoreCase(region);
        }

        if (countries.isEmpty()) {
            logger.info("No countries found for region: {}", region);
        } else {
            logger.info("Fetched {} countries for region: {}", countries.size(), region);
        }

        return countries.stream()
                .map(country -> new CountryDTO(
                        country.getId(),
                        country.getName(),
                        country.getCode(),
                        country.getRegion()
                ))
                .collect(Collectors.toList());
    }

    public List<CountryDTO> getCountriesByRegionFromGeoNames(String region) {
        logger.info("Fetching countries by region from GeoNames API: {}", region);
        return getAllCountriesFromGeoNames().stream()
                .filter(country -> region.equalsIgnoreCase(country.region()))
                .collect(Collectors.toList());
    }

    public void populateCountries() {
        logger.info("Populating countries from GeoNames.");
        String geoNamesUrl = String.format("http://api.geonames.org/countryInfoJSON?username=%s", geonamesUsername);

        try {
            Map<String, Object> response = restTemplate.getForObject(geoNamesUrl, Map.class);
            if (response == null || !response.containsKey("geonames")) {
                logger.warn("No data returned from GeoNames.");
                return;
            }

            List<Map<String, Object>> geoNamesCountries = (List<Map<String, Object>>) response.get("geonames");

            geoNamesCountries.forEach(countryData -> {
                String name = (String) countryData.get("countryName");
                String code = (String) countryData.get("countryCode");
                String continent = (String) countryData.get("continent");

                if (continent == null) {
                    continent = "Unknown";
                }

                if (!countryRepository.existsByName(name)) {
                    Country country = new Country();
                    country.setName(name);
                    country.setCode(code);
                    country.setRegion(continent);
                    countryRepository.save(country);
                    logger.info("Saved country: {} ({}), continent: {}", name, code, continent);
                }
            });
        } catch (Exception e) {
            logger.error("Error populating countries: {}", e.getMessage());
        }
    }


    private Country findCountryByIdentifier(String identifier) {
        if (isUUID(identifier)) {
            return countryRepository.findById(UUID.fromString(identifier))
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with ID: " + identifier));
        } else if (identifier.length() == 2) {
            return countryRepository.findByCode(identifier)
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with code: " + identifier));
        } else {
            return countryRepository.findByName(identifier)
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with name: " + identifier));
        }
    }

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

    private CountryDetailsDTO.MarkerInfo createMarkerInfo(List<?> latlng) {
        if (latlng == null || latlng.size() < 2) {
            return null;
        }
        return CountryDetailsDTO.MarkerInfo.builder()
                .latitude(((Number) latlng.get(0)).doubleValue())
                .longitude(((Number) latlng.get(1)).doubleValue())
                .build();
    }

    private boolean isUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public List<CountryDTO> searchCountries(String query) {
        List<CountryDTO> countriesFromDB = countryRepository.findByNameIgnoreCase(query).stream()
                .map(country -> new CountryDTO(
                        country.getId(),
                        country.getName(),
                        country.getCode(),
                        country.getRegion()
                ))
                .collect(Collectors.toList());

        if (!countriesFromDB.isEmpty()) {
            return countriesFromDB;
        }

        countriesFromDB = countryRepository.findByCodeIgnoreCase(query).stream()
                .map(country -> new CountryDTO(
                        country.getId(),
                        country.getName(),
                        country.getCode(),
                        country.getRegion()
                ))
                .collect(Collectors.toList());

        return countriesFromDB.isEmpty() ? fetchCountriesFromGeoNames(query) : countriesFromDB;
    }


    private List<CountryDTO> fetchCountriesFromGeoNames(String query) {
        String url = String.format("http://api.geonames.org/searchJSON?q=%s&featureClass=A&maxRows=15&username=%s", query, geonamesUsername);

        logger.info("Fetching countries from GeoNames for query: {}", query);

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
            logger.error("Error fetching countries from GeoNames: {}", e.getMessage());
        }

        return List.of();
    }
}
