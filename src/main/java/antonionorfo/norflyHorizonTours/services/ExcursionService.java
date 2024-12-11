package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Country;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import antonionorfo.norflyHorizonTours.repositories.CityRepository;
import antonionorfo.norflyHorizonTours.repositories.CountryRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcursionService {

    private static final Logger logger = LoggerFactory.getLogger(ExcursionService.class);

    private static final int EXCURSIONS_PER_CITY = 40;
    private static final int EXCURSIONS_PER_COUNTRY = 40;
    private static final int EXCURSIONS_PER_REGION = 40;

    private final ExcursionRepository excursionRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final AvailabilityService availabilityService;
    private final BookingRepository bookingRepository;
    private final Faker faker = new Faker();

    public boolean isExcursionsPopulated() {
        long totalCityExcursions = excursionRepository.countByCityNotNull();
        long totalCountryExcursions = excursionRepository.countByCountryNotNull();
        long totalRegionExcursions = excursionRepository.countByRegionNotNull();

        long requiredCityExcursions = cityRepository.count() * EXCURSIONS_PER_CITY;
        long requiredCountryExcursions = countryRepository.count() * EXCURSIONS_PER_COUNTRY;
        long requiredRegionExcursions = (long) countryRepository.findDistinctRegions().size() * EXCURSIONS_PER_REGION;

        if (totalCityExcursions >= requiredCityExcursions &&
                totalCountryExcursions >= requiredCountryExcursions &&
                totalRegionExcursions >= requiredRegionExcursions) {
            logger.info("Il DB è già popolato con il numero richiesto di escursioni.");
            return true;
        }
        return false;
    }


    public void generateAllExcursions() {
        logger.info("Inizio la generazione delle escursioni per città, paesi e regioni...");

        generateExcursionsForCities();
        generateExcursionsForCountries();
        generateExcursionsForRegions();

        logger.info("Completata la generazione delle escursioni.");
    }

    private void generateExcursionsForCities() {
        logger.info("Inizio la generazione delle escursioni per le città...");

        List<City> cities = cityRepository.findAll();

        cities.forEach(city -> {
            try {
                logger.info("Generando escursioni per la città: {}", city.getName());
                List<Excursion> cityExcursions = createExcursionsForCity(city);
                excursionRepository.saveAll(cityExcursions);

                cityExcursions.forEach(availabilityService::generateDefaultAvailabilityForExcursion);

                logger.info("Salvate {} escursioni per la città: {}", cityExcursions.size(), city.getName());
            } catch (Exception e) {
                logger.error("Errore nella generazione delle escursioni per la città: {} - {}", city.getName(), e.getMessage());
            }
        });

        logger.info("Completata la generazione delle escursioni per le città.");
    }

    private void generateExcursionsForCountries() {
        logger.info("Inizio la generazione delle escursioni per i paesi...");

        List<Country> countries = countryRepository.findAll();

        countries.forEach(country -> {
            try {
                logger.info("Generando escursioni per il paese: {}", country.getName());
                List<Excursion> countryExcursions = createExcursionsForCountry(country);
                excursionRepository.saveAll(countryExcursions);

                countryExcursions.forEach(availabilityService::generateDefaultAvailabilityForExcursion);

                logger.info("Salvate {} escursioni per il paese: {}", countryExcursions.size(), country.getName());
            } catch (Exception e) {
                logger.error("Errore nella generazione delle escursioni per il paese: {} - {}", country.getName(), e.getMessage());
            }
        });

        logger.info("Completata la generazione delle escursioni per i paesi.");
    }


    private void generateExcursionsForRegions() {
        logger.info("Inizio la generazione delle escursioni per le regioni...");

        countryRepository.findAll().stream()
                .collect(Collectors.groupingBy(Country::getRegion))
                .forEach((region, regionCountries) -> {
                    try {
                        logger.info("Generando escursioni per la regione: {}", region);
                        List<Excursion> regionExcursions = createExcursionsForRegion(region);
                        excursionRepository.saveAll(regionExcursions);

                        regionExcursions.forEach(availabilityService::generateDefaultAvailabilityForExcursion);

                        logger.info("Salvate {} escursioni per la regione: {}", regionExcursions.size());
                    } catch (Exception e) {
                        logger.error("Errore nella generazione delle escursioni per la regione: {} - {}", region, e.getMessage());
                    }
                });

        logger.info("Completata la generazione delle escursioni per le regioni.");
    }


    private List<Excursion> createExcursionsForCity(City city) {
        List<Excursion> excursions = new ArrayList<>();
        int hourlyExcursionsCount = 5;
        int dailyExcursionsCount = 5;
        List<String> hourlyDurations = List.of("3 hours", "5 hours", "7 hours", "10 hours");

        for (int i = 0; i < hourlyExcursionsCount; i++) {
            String randomDuration = faker.options().nextElement(hourlyDurations);
            Excursion excursion = buildExcursionTemplate(randomDuration);
            excursion.setCity(city);
            excursion.setCountry(city.getCountry());
            excursions.add(excursion);
        }

        for (int i = 0; i < dailyExcursionsCount; i++) {
            Excursion excursion = buildExcursionTemplate("1 day");
            excursion.setCity(city);
            excursion.setCountry(city.getCountry());
            excursions.add(excursion);
        }

        return excursions;
    }


    private List<Excursion> createExcursionsForCountry(Country country) {
        List<Excursion> excursions = new ArrayList<>();
        logger.info("Generazione escursioni per il paese: {}", country.getName());
        int hourlyExcursionsCount = 5;
        int dailyExcursionsCount = 5;
        List<String> hourlyDurations = List.of("3 hours", "5 hours", "7 hours", "10 hours", "15 hours");

        for (int i = 0; i < hourlyExcursionsCount; i++) {
            String randomDuration = faker.options().nextElement(hourlyDurations);
            Excursion excursion = buildExcursionTemplate(randomDuration);
            excursion.setCountry(country);
            excursion.setCity(null);
            excursions.add(excursion);
        }

        for (int i = 0; i < dailyExcursionsCount; i++) {
            Excursion excursion = buildExcursionTemplate("1 day");
            excursion.setCountry(country);
            excursion.setCity(null);
            excursions.add(excursion);
        }

        logger.info("Generazione completata per {} escursioni per il paese {}", excursions.size(), country.getName());
        return excursions;
    }


    private List<Excursion> createExcursionsForRegion(String region) {
        List<Excursion> excursions = new ArrayList<>();
        logger.info("Generazione escursioni per la regione: {}", region);
        int hourlyExcursionsCount = 5;
        int dailyExcursionsCount = 5;
        List<String> hourlyDurations = List.of("3 hours", "5 hours", "7 hours", "10 hours", "15 hours");

        for (int i = 0; i < hourlyExcursionsCount; i++) {
            String randomDuration = faker.options().nextElement(hourlyDurations);
            Excursion excursion = buildExcursionTemplate(randomDuration);
            excursion.setDescriptionExcursion("Excursion specifica per la regione: " + region);
            excursions.add(excursion);
        }

        for (int i = 0; i < dailyExcursionsCount; i++) {
            Excursion excursion = buildExcursionTemplate("1 day");
            excursion.setDescriptionExcursion("Excursion specifica per la regione: " + region);
            excursions.add(excursion);
        }

        logger.info("Generazione completata per {} escursioni per la regione {}", excursions.size(), region);
        return excursions;
    }


    private Excursion createExcursion(City city, String duration) {
        Excursion excursion = buildExcursionTemplate(duration);
        excursion.setCity(city);
        return excursionRepository.save(excursion);
    }

    private Excursion createExcursionForCountry(Country country, String duration) {
        Excursion excursion = buildExcursionTemplate(duration);
        excursion.setCountry(country);
        excursion.setCity(null);
        return excursion;
    }


    private Excursion createExcursionForRegion(String region, String duration) {
        Excursion excursion = buildExcursionTemplate(duration);
        excursion.setDescriptionExcursion("Region-based excursion for " + region);
        return excursionRepository.save(excursion);
    }

    private Excursion buildExcursionTemplate(String duration) {
        Excursion excursion = new Excursion();

        excursion.setExcursionId(UUID.randomUUID());
        excursion.setTitle(faker.commerce().productName());

        excursion.setDescriptionExcursion(
                "Experience the " + faker.lorem().words(3).stream().reduce((a, b) -> a + " " + b).orElse("") +
                        " with " + faker.company().name() + ". " +
                        faker.lorem().sentence(30)
        );

        excursion.setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 300)));

        excursion.setDuration(duration);

        excursion.setDifficultyLevel(DifficultyLevel.values()[faker.random().nextInt(DifficultyLevel.values().length)]);

        excursion.setInclusions(
                "Included: " + faker.commerce().material() + ", " + faker.food().dish() + ", and " +
                        "a free gift from " + faker.company().name() + "."
        );

        excursion.setRules(
                "Rules: Please remember to " + faker.lorem().sentence(5) + ". " +
                        "Always carry " + faker.commerce().productName() + " provided by our team."
        );
        excursion.setNotRecommended(
                "Not recommended for: " + faker.medical().diseaseName() + " patients, " +
                        "people with " + faker.medical().symptoms() + ", or those sensitive to " +
                        faker.lorem().word() + " environments."
        );
        excursion.setMaxParticipants(faker.number().numberBetween(5, 30));

        return excursion;
    }


    public List<ExcursionDTO> getExcursionsByCity(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Città non trovata"));
        return excursionRepository.findByCity(city).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExcursionDTO> findExcursionsByCityName(String cityName) {
        logger.info("Fetching excursions for city: {}", cityName);
        List<Excursion> excursions = excursionRepository.findByCity_NameIgnoreCase(cityName);
        if (excursions.isEmpty()) {
            logger.warn("No excursions found for city: {}", cityName);
        }
        return excursions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ExcursionDTO> findExcursionsByCountry(String countryName) {
        logger.info("Fetching excursions for country: {}", countryName);
        List<City> cities = cityRepository.findByCountry_NameIgnoreCase(countryName);
        return cities.stream()
                .flatMap(city -> excursionRepository.findByCity(city).stream())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExcursionDTO> findExcursionsByRegion(String region) {
        return cityRepository.findByCountry_Region(region).stream()
                .flatMap(city -> excursionRepository.findByCity(city).stream())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExcursionDTO> findExcursionsByFilters(String cityName, DifficultyLevel difficulty, BigDecimal minPrice, BigDecimal maxPrice) {
        return excursionRepository.findByCity_NameIgnoreCase(cityName).stream()
                .filter(excursion -> (difficulty == null || excursion.getDifficultyLevel() == difficulty))
                .filter(excursion -> (minPrice == null || excursion.getPrice().compareTo(minPrice) >= 0))
                .filter(excursion -> (maxPrice == null || excursion.getPrice().compareTo(maxPrice) <= 0))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ExcursionDTO> getAllExcursionsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Excursion> excursions = excursionRepository.findAll(pageable);
        return excursions.map(this::mapToDTO);
    }

    private ExcursionDTO mapToDTO(Excursion excursion) {
        return new ExcursionDTO(
                excursion.getExcursionId(),
                excursion.getTitle(),
                excursion.getDescriptionExcursion(),
                excursion.getPrice(),
                excursion.getDuration(),
                excursion.getDifficultyLevel(),
                excursion.getInclusions(),
                excursion.getRules(),
                excursion.getNotRecommended() != null ? excursion.getNotRecommended() : "",
                excursion.getMaxParticipants(),
                excursion.getCity() != null ? excursion.getCity().getId() : null,
                excursion.getCountry() != null ? excursion.getCountry().getId() : null,
                excursion.getMarkers()
        );
    }

    public long getTotalBookingsForExcursion(UUID excursionId) {
        return bookingRepository.countByExcursion_ExcursionId(excursionId);
    }
}
