package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Country;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import antonionorfo.norflyHorizonTours.payloads.CityDTO;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO.MarkerInfo;
import antonionorfo.norflyHorizonTours.repositories.CityRepository;
import antonionorfo.norflyHorizonTours.repositories.CountryRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);

    private final RestTemplate restTemplate;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final ExcursionRepository excursionRepository;
    private final Faker faker = new Faker();

    @Value("${geonames.username}")
    private String geonamesUsername;

    public List<CityDTO> getCitiesByCountryFromDB(String countryIdentifier) {
        logger.info("Fetching cities from DB for country identifier: {}", countryIdentifier);

        final Country country;

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

        return cityRepository.findByCountry(country).stream()
                .map(city -> new CityDTO(city.getId(), city.getName(), country.getId(), city.getDescription(), city.getCoordinates()))
                .collect(Collectors.toList());
    }

    private boolean isUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public List<CityDTO> searchCities(String countryCode, String cityName) {
        List<CityDTO> citiesFromDB = cityRepository.findByName(cityName).stream()
                .filter(city -> city.getCountry().getCode().equalsIgnoreCase(countryCode))
                .map(city -> new CityDTO(city.getId(), city.getName(), city.getCountry().getId(), city.getDescription(), city.getCoordinates()))
                .collect(Collectors.toList());

        if (!citiesFromDB.isEmpty()) {
            return citiesFromDB;
        }

        return fetchCitiesFromGeoNames(countryCode, cityName);
    }

    private List<CityDTO> fetchCitiesFromGeoNames(String countryCode, String cityName) {
        String url = String.format("http://api.geonames.org/searchJSON?q=%s&country=%s&featureClass=P&maxRows=15&username=%s",
                cityName, countryCode, geonamesUsername);

        logger.info("Fetching cities from GeoNames for countryCode: {} and cityName: {}", countryCode, cityName);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("geonames")) {
                return ((List<Map<String, Object>>) response.get("geonames")).stream()
                        .map(data -> new CityDTO(
                                null,
                                (String) data.get("name"),
                                null,
                                "Description not available",
                                data.get("lat") + "," + data.get("lng")
                        ))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error fetching cities from GeoNames: {}", e.getMessage());
        }

        return List.of();
    }

    public List<CityDTO> getCitiesByCountryFromGeoNames(String countryCode) {
        String url = String.format("http://api.geonames.org/searchJSON?country=%s&featureClass=P&maxRows=15&username=%s",
                countryCode, geonamesUsername);
        logger.info("Fetching cities from GeoNames API for country code: {}", countryCode);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("geonames")) {
                return ((List<Map<String, Object>>) response.get("geonames")).stream()
                        .map(data -> new CityDTO(null, (String) data.get("name"), null, "Description not available",
                                data.get("lat") + "," + data.get("lng"))).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error fetching cities from GeoNames API.", e);
        }

        throw new IllegalArgumentException("Cities not found for country code: " + countryCode);
    }

    public List<CityDTO> getCitiesByRegionFromDB(String region) {
        logger.info("Fetching cities from DB for region: {}", region);

        List<Country> countries = countryRepository.findByRegion(region);
        if (countries.isEmpty()) {
            logger.warn("No countries found for region: {}", region);
            return List.of();
        } else {
            logger.info("Found {} countries in region: {}", countries.size(), region);
        }

        List<CityDTO> cities = countries.stream()
                .flatMap(country -> {
                    List<City> countryCities = cityRepository.findByCountry(country);
                    logger.info("Country '{}' has {} cities", country.getName(), countryCities.size());
                    return countryCities.stream();
                })
                .map(city -> new CityDTO(city.getId(), city.getName(), city.getCountry().getId(), null, city.getCoordinates()))
                .collect(Collectors.toList());

        logger.info("Total cities found for region {}: {}", region, cities.size());
        return cities;
    }

    public List<CityDTO> getCitiesByRegionFromGeoNames(String region) {
        logger.info("Fetching cities from GeoNames API for region: {}", region);
        List<Map<String, String>> countries = fetchCountriesByRegion(region);

        return countries.stream()
                .flatMap(country -> getCitiesByCountryFromGeoNames(country.get("code")).stream())
                .collect(Collectors.toList());
    }

    private List<Map<String, String>> fetchCountriesByRegion(String region) {
        String url = "https://restcountries.com/v3.1/all";
        logger.info("Fetching countries by region from GeoNames API: {}", region);

        try {
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            if (response == null) {
                throw new IllegalArgumentException("No countries found for region: " + region);
            }

            return response.stream()
                    .filter(country -> region.equalsIgnoreCase((String) country.get("region")))
                    .map(country -> Map.of(
                            "name", (String) ((Map<String, Object>) country.get("name")).get("common"),
                            "code", (String) country.get("cca2"),
                            "region", region))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching countries by region.", e);
            throw new RuntimeException("Error while fetching countries by region", e);
        }
    }

    public CountryDetailsDTO fetchCountryDetails(String countryCode) {
        String url = String.format("https://restcountries.com/v3.1/alpha/%s", countryCode);
        logger.info("Fetching details for country code: {}", countryCode);

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
            logger.error("Error fetching details for country code: {}", e.getMessage());
            throw new RuntimeException("Error while fetching country details", e);
        }
    }

    public void populateCities() {
        logger.info("Popolamento delle città nel DB da GeoNames.");
        if (cityRepository.count() == 0) {
            logger.info("Il DB delle città è vuoto, iniziando il popolamento.");
            countryRepository.findAll().forEach(country -> {
                try {
                    List<CityDTO> cities = getCitiesByCountryFromGeoNames(country.getCode());
                    cities.forEach(cityDTO -> {
                        if (!cityRepository.existsByNameAndCountry(cityDTO.name(), country)) {
                            City city = new City();
                            city.setName(cityDTO.name());
                            city.setCountry(country);
                            city.setCoordinates(cityDTO.coordinates());
                            cityRepository.save(city);
                            logger.info("Città salvata: {} in Paese: {}", city.getName(), country.getName());
                        }
                    });
                } catch (Exception e) {
                    logger.error("Errore durante il popolamento delle città per il Paese: {}", country.getName(), e);
                }
            });
        } else {
            logger.info("Il DB delle città è già popolato.");
        }
    }

    public void generateExcursionsForCities() {
        List<City> cities = cityRepository.findAll();

        for (City city : cities) {
            logger.info("Generating excursions for city: {}", city.getName());
            List<Excursion> excursions = createExcursionsForCity(city);
            excursionRepository.saveAll(excursions);
        }
    }

    private List<Excursion> createExcursionsForCity(City city) {
        List<Excursion> excursions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Excursion excursion = new Excursion();
            excursion.setTitle(faker.commerce().productName() + " in " + city.getName());
            excursion.setDescriptionExcursion(faker.lorem().sentence(20));
            excursion.setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 300)));
            excursion.setDuration(faker.number().numberBetween(2, 8) + " hours");
            excursion.setDifficultyLevel(DifficultyLevel.values()[faker.random().nextInt(DifficultyLevel.values().length)]);
            excursion.setInclusions("Guide, Tickets, Transport");
            excursion.setRules("Follow guide instructions, bring water.");
            excursion.setNotRecommended("Pregnant women, people with heart conditions");
            excursion.setMaxParticipants(faker.number().numberBetween(5, 30));
            excursion.setCity(city);
            excursions.add(excursion);
        }

        return excursions;
    }

    private String extractCurrency(Map<String, Object> country) {
        if (!country.containsKey("currencies")) {
            return null;
        }
        Map<String, Map<String, String>> currencies = (Map<String, Map<String, String>>) country.get("currencies");
        Map.Entry<String, Map<String, String>> entry = currencies.entrySet().iterator().next();
        return entry.getValue().get("name") + " (" + entry.getValue().get("symbol") + ")";
    }

    private MarkerInfo createMarkerInfo(List<?> latlng) {
        if (latlng == null || latlng.size() < 2) {
            throw new IllegalArgumentException("Invalid latitude and longitude data");
        }
        return new MarkerInfo(toDouble(latlng.get(0)), toDouble(latlng.get(1)));
    }

    private Long toLong(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return null;
    }

    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
}
