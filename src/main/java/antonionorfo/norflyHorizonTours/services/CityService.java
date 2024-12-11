package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Country;
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

import java.util.*;
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

    private boolean isUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public List<CityDTO> getCitiesByCountryFromDB(String identifier) {
        logger.info("Fetching cities from DB for identifier: {}", identifier);

        List<City> citiesByName = cityRepository.findByName(identifier);
        if (!citiesByName.isEmpty()) {
            return citiesByName.stream()
                    .map(this::convertToCityDTO)
                    .collect(Collectors.toList());
        }

        Country country = findCountryByIdentifier(identifier);

        return cityRepository.findByCountry(country).stream()
                .map(this::convertToCityDTO)
                .collect(Collectors.toList());
    }

    public List<CityDTO> searchCities(String countryCode, String cityName) {
        List<CityDTO> citiesFromDB = cityRepository.findByName(cityName).stream()
                .filter(city -> city.getCountry().getCode().equalsIgnoreCase(countryCode))
                .map(this::convertToCityDTO)
                .collect(Collectors.toList());

        if (!citiesFromDB.isEmpty()) {
            return citiesFromDB;
        }

        return fetchCitiesFromGeoNames(countryCode, cityName);
    }

    public List<CityDTO> getCitiesByCountryFromGeoNames(String countryCode, String countryName) {
        logger.info("Inizio recupero città principali da GeoNames per il paese con codice: '{}' o nome: '{}'", countryCode, countryName);

        List<CityDTO> allCities = new ArrayList<>();
        boolean dataFound = false;

        try {
            List<CityDTO> citiesByCode = fetchCitiesFromGeoNames(countryCode, "country");
            logger.debug("Città trovate per codice '{}': {}", countryCode, citiesByCode);

            if (!citiesByCode.isEmpty()) {
                allCities.addAll(citiesByCode);
                dataFound = true;
            }

            if (!dataFound) {
                logger.warn("Nessun dato trovato per il codice del paese '{}'. Tentativo con il nome del paese '{}'", countryCode, countryName);
                List<CityDTO> citiesByName = fetchCitiesFromGeoNames(countryName, "q");
                logger.debug("Città trovate per nome '{}': {}", countryName, citiesByName);

                if (!citiesByName.isEmpty()) {
                    allCities.addAll(citiesByName);
                    dataFound = true;
                }
            }

            if (!dataFound) {
                logger.warn("Nessun dato trovato per il paese con codice '{}' e nome '{}'", countryCode, countryName);
            }
        } catch (Exception e) {
            logger.error("Errore durante il recupero delle città da GeoNames per codice '{}' e nome '{}': {}", countryCode, countryName, e.getMessage(), e);
        }

        logger.info("Recupero completato. Numero di città trovate: {}", allCities.size());
        return allCities;
    }


    private List<CityDTO> fetchCitiesFromGeoNames(String query, String queryType) {
        String baseUrl = "http://api.geonames.org/searchJSON";
        int rowsPerPage = 60;
        int maxCities = 15;
        List<CityDTO> allCities = new ArrayList<>();

        String url = String.format(
                "%s?%s=%s&featureClass=P&maxRows=%d&orderby=population&username=%s",
                baseUrl, queryType, query, rowsPerPage, geonamesUsername);

        try {
            logger.info("Eseguendo richiesta GeoNames: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("geonames")) {
                logger.warn("Nessun dato ricevuto o errore nella risposta per query '{}'", query);
                return allCities;
            }

            List<Map<String, Object>> cities = (List<Map<String, Object>>) response.get("geonames");
            if (cities.isEmpty()) {
                logger.info("Nessuna città trovata nella risposta per query '{}'.", query);
                return allCities;
            }

            for (Map<String, Object> cityData : cities) {
                if (allCities.size() >= maxCities) break;
                CityDTO cityDTO = convertGeoNamesDataToCityDTO(cityData);
                if (cityDTO != null) {
                    allCities.add(cityDTO);
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante la richiesta GeoNames per query '{}': {}", query, e.getMessage(), e);
        }

        logger.info("Totale città recuperate per query '{}': {}", query, allCities.size());
        return allCities;
    }


    public List<CityDTO> getCitiesByRegionFromDB(String region) {
        logger.info("Fetching cities from DB for region: {}", region);

        List<Country> countries = countryRepository.findByRegion(region);
        if (countries.isEmpty()) {
            logger.warn("No countries found for region: {}", region);
            return List.of();
        }

        return countries.stream()
                .flatMap(country -> cityRepository.findByCountry(country).stream())
                .map(this::convertToCityDTO)
                .collect(Collectors.toList());
    }

    public List<CityDTO> getCitiesByRegionFromGeoNames(String region) {
        logger.info("Fetching cities from GeoNames API for region: {}", region);
        List<Map<String, String>> countries = fetchCountriesByRegion(region);

        return countries.stream()
                .flatMap(country -> {
                    String countryCode = country.get("code");
                    String countryName = country.get("name");
                    return getCitiesByCountryFromGeoNames(countryCode, countryName).stream();
                })
                .collect(Collectors.toList());
    }


    private List<Map<String, String>> fetchCountriesByRegion(String region) {
        String url = "https://restcountries.com/v3.1/all";
        logger.info("Fetching countries by region from RestCountries API: {}", region);

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
                throw new IllegalStateException("Invalid response from RestCountries API");
            }

            Map<String, Object> country = response.get(0);

            return CountryDetailsDTO.builder()
                    .name(getNestedValue(country, "name", "common"))
                    .officialName(getNestedValue(country, "name", "official"))
                    .capital(getListValue(country, "capital", 0))
                    .population(toLong(country.get("population")))
                    .area(toDouble(country.get("area")))
                    .region((String) country.get("region"))
                    .subregion((String) country.get("subregion"))
                    .languages(getLanguages(country))
                    .currency(extractCurrency(country))
                    .flag((String) country.get("flag"))
                    .maps((Map<String, String>) country.get("maps"))
                    .markerInfo(createMarkerInfo((List<?>) country.get("latlng")))
                    .build();
        } catch (Exception e) {
            logger.error("Error fetching details for country code: {}", e.getMessage(), e);
            throw new RuntimeException("Error while fetching country details", e);
        }
    }

    public void populateCities() {
        logger.info("Inizio popolamento delle città principali per i paesi nel database...");

        Map<String, List<CityDTO>> countryCityCache = new HashMap<>();

        List<Country> countries = countryRepository.findAll();
        if (countries.isEmpty()) {
            logger.warn("Nessun paese trovato nel database. Impossibile popolare le città.");
            return;
        }

        countries.forEach(country -> {
            try {
                logger.info("Inizio popolamento delle città per il paese: {} ({})", country.getName(), country.getCode());

                List<CityDTO> citiesFromGeoNames = countryCityCache.computeIfAbsent(
                        country.getCode(),
                        code -> getCitiesByCountryFromGeoNames(country.getCode(), country.getName())
                );

                if (citiesFromGeoNames.isEmpty()) {
                    logger.warn("Nessun dato ricevuto dall'API GeoNames per il paese: {} ({})", country.getName(), country.getCode());
                    return;
                }

                Set<String> usedCodes = cityRepository.findByCountry(country).stream()
                        .map(City::getCode)
                        .collect(Collectors.toSet());

                int citiesToSaveCount = Math.min(Math.max(5, citiesFromGeoNames.size()), 15);
                List<CityDTO> limitedCities = citiesFromGeoNames.subList(0, Math.min(citiesToSaveCount, citiesFromGeoNames.size()));

                List<City> newCities = limitedCities.stream()
                        .filter(cityDTO -> cityDTO.name() != null && !usedCodes.contains(cityDTO.code()))
                        .map(cityDTO -> {
                            City city = new City();
                            city.setName(cityDTO.name());
                            city.setCountry(country);
                            city.setCode(cityDTO.code());
                            city.setDescription(Optional.ofNullable(cityDTO.description())
                                    .map(desc -> desc.length() > 255 ? desc.substring(0, 255) : desc)
                                    .orElse("Città rilevante non documentata"));
                            city.setCoordinates(Optional.ofNullable(cityDTO.coordinates())
                                    .map(coord -> coord.length() > 255 ? coord.substring(0, 255) : coord)
                                    .orElse("0,0"));
                            return city;
                        })
                        .collect(Collectors.toList());

                if (!newCities.isEmpty()) {
                    cityRepository.saveAll(newCities);
                    logger.info("{} nuove città salvate per il paese: {} ({})", newCities.size(), country.getName(), country.getCode());
                } else {
                    logger.info("Nessuna nuova città salvata per il paese: {} ({}) - Tutte le città erano già presenti.", country.getName(), country.getCode());
                }
            } catch (Exception e) {
                logger.error("Errore nel popolamento delle città per il paese {}: {}", country.getName(), e.getMessage(), e);
            }
        });

        logger.info("Popolamento delle città completato.");
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
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private List<String> getLanguages(Map<String, Object> country) {
        if (country.containsKey("languages")) {
            return new ArrayList<>(((Map<String, String>) country.get("languages")).values());
        }
        return List.of();
    }

    private String getNestedValue(Map<String, Object> map, String key1, String key2) {
        Map<String, String> nestedMap = (Map<String, String>) map.get(key1);
        return nestedMap != null ? nestedMap.get(key2) : null;
    }

    private String getListValue(Map<String, Object> map, String key, int index) {
        List<String> list = (List<String>) map.get(key);
        return list != null && list.size() > index ? list.get(index) : null;
    }

    private CityDTO convertToCityDTO(City city) {
        return new CityDTO(
                city.getId(),
                city.getName(),
                city.getCountry().getId(),
                city.getDescription(),
                city.getCoordinates(),
                city.getCode()
        );
    }

    private CityDTO convertGeoNamesDataToCityDTO(Map<String, Object> data) {
        try {
            logger.debug("Elaborazione dei dati città: {}", data);
            String name = (String) data.get("name");
            if (name == null || name.isEmpty()) {
                logger.warn("Skipping city with missing name: {}", data);
                return null;
            }
            CityDTO cityDTO = new CityDTO(
                    null,
                    name,
                    null,
                    faker.lorem().paragraph(3),
                    String.format("%s,%s", data.getOrDefault("lat", "0"), data.getOrDefault("lng", "0")),
                    String.valueOf(data.getOrDefault("geonameId", "unknown"))
            );
            logger.debug("CityDTO convertito con successo: {}", cityDTO);
            return cityDTO;
        } catch (Exception e) {
            logger.error("Errore durante la conversione dei dati città: {}", data, e);
            return null;
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
}
