package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.AvailabilityDate;
import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Country;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.repositories.*;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcursionService {

    private static final Logger logger = LoggerFactory.getLogger(ExcursionService.class);

    private static final int EXCURSIONS_PER_CITY = 40; // 10 da 3-10 ore, 10 di 1 giorno, 10 di 3 giorni, 10 di 1 settimana
    private static final int EXCURSIONS_PER_REGION = 60; // 15 da 3-15 ore, 15 di 1 giorno, 15 di 3 giorni, 15 di 1 settimana
    private static final int EXCURSIONS_PER_COUNTRY = 60; // 15 da 3-15 ore, 15 di 1 giorno, 15 di 3 giorni, 15 di 1 settimana

    private final ExcursionRepository excursionRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final AvailabilityDateRepository availabilityDateRepository;
    private final Faker faker = new Faker();
    private final BookingRepository bookingRepository;

    // Verifica se le escursioni sono già state popolate nel database
    public boolean isExcursionsPopulated() {
        long totalExcursions = excursionRepository.count();
        long requiredExcursions = cityRepository.count() * EXCURSIONS_PER_CITY;

        if (totalExcursions >= requiredExcursions) {
            logger.info("Il DB è già popolato con il numero richiesto di escursioni.");
            return true;
        }
        return false;
    }

    // Metodo principale per generare escursioni per città, paesi e regioni
    public void generateExcursionsForCities() {
        if (isExcursionsPopulated()) {
            return;
        }

        logger.info("Inizio la generazione delle escursioni per tutte le città, regioni e paesi...");
        List<City> cities = cityRepository.findAll();
        List<Country> countries = countryRepository.findAll();

        // Genera escursioni per tutte le città
        cities.forEach(city -> {
            try {
                logger.info("Generando escursioni per la città: {}", city.getName());
                List<Excursion> cityExcursions = createExcursionsForCity(city);
                excursionRepository.saveAll(cityExcursions);
                cityExcursions.forEach(this::generateAvailabilityForExcursion);
            } catch (Exception e) {
                logger.error("Errore nella generazione delle escursioni per la città: {} - {}", city.getName(), e.getMessage());
            }
        });

        // Genera escursioni per tutti i paesi
        countries.forEach(country -> {
            try {
                logger.info("Generando escursioni per il paese: {}", country.getName());
                List<Excursion> countryExcursions = createExcursionsForCountry(country);
                excursionRepository.saveAll(countryExcursions);
                countryExcursions.forEach(this::generateAvailabilityForExcursion);
            } catch (Exception e) {
                logger.error("Errore nella generazione delle escursioni per il paese: {} - {}", country.getName(), e.getMessage());
            }
        });

        // Genera escursioni per tutte le regioni
        countries.stream()
                .collect(Collectors.groupingBy(country -> country.getRegion()))
                .forEach((region, regionCountries) -> {
                    regionCountries.forEach(country -> {
                        try {
                            logger.info("Generando escursioni per la regione: {}", region);
                            List<Excursion> regionExcursions = createExcursionsForRegion(region);
                            excursionRepository.saveAll(regionExcursions);
                            regionExcursions.forEach(this::generateAvailabilityForExcursion);
                        } catch (Exception e) {
                            logger.error("Errore nella generazione delle escursioni per la regione: {} - {}", region, e.getMessage());
                        }
                    });
                });
    }

    // Funzione per creare escursioni per una città
    private List<Excursion> createExcursionsForCity(City city) {
        List<Excursion> excursions = new ArrayList<>();

        // 10 escursioni da 3-10 ore
        for (int i = 0; i < 10; i++) {
            excursions.add(createExcursion(city, faker.options().option("3 hours", "5 hours", "7 hours", "10 hours")));
        }

        // 10 escursioni di 1 giorno
        for (int i = 10; i < 20; i++) {
            excursions.add(createExcursion(city, "1 day"));
        }

        // 10 escursioni di 3 giorni
        for (int i = 20; i < 30; i++) {
            excursions.add(createExcursion(city, "3 days"));
        }

        // 10 escursioni di 1 settimana
        for (int i = 30; i < 40; i++) {
            excursions.add(createExcursion(city, "1 week"));
        }

        return excursions;
    }

    // Funzione per creare escursioni per un paese
    private List<Excursion> createExcursionsForCountry(Country country) {
        List<Excursion> excursions = new ArrayList<>();

        // 15 escursioni da 3-15 ore
        for (int i = 0; i < 15; i++) {
            excursions.add(createExcursionForCountry(country, faker.options().option("3 hours", "5 hours", "7 hours", "10 hours", "15 hours")));
        }

        // 15 escursioni di 1 giorno
        for (int i = 15; i < 30; i++) {
            excursions.add(createExcursionForCountry(country, "1 day"));
        }

        // 15 escursioni di 3 giorni
        for (int i = 30; i < 45; i++) {
            excursions.add(createExcursionForCountry(country, "3 days"));
        }

        // 15 escursioni di 1 settimana
        for (int i = 45; i < 60; i++) {
            excursions.add(createExcursionForCountry(country, "1 week"));
        }

        return excursions;
    }

    // Funzione per creare escursioni per una regione
    private List<Excursion> createExcursionsForRegion(String region) {
        List<Excursion> excursions = new ArrayList<>();

        // 15 escursioni da 3-15 ore
        for (int i = 0; i < 15; i++) {
            excursions.add(createExcursionForRegion(region, faker.options().option("3 hours", "5 hours", "7 hours", "10 hours", "15 hours")));
        }

        // 15 escursioni di 1 giorno
        for (int i = 15; i < 30; i++) {
            excursions.add(createExcursionForRegion(region, "1 day"));
        }

        // 15 escursioni di 3 giorni
        for (int i = 30; i < 45; i++) {
            excursions.add(createExcursionForRegion(region, "3 days"));
        }

        // 15 escursioni di 1 settimana
        for (int i = 45; i < 60; i++) {
            excursions.add(createExcursionForRegion(region, "1 week"));
        }

        return excursions;
    }

    // Funzione per creare una singola escursione per città
    private Excursion createExcursion(City city, String duration) {
        Excursion excursion = new Excursion();
        excursion.setExcursionId(UUID.randomUUID());
        excursion.setTitle(faker.commerce().productName() + " in " + city.getName());
        excursion.setDescriptionExcursion(faker.lorem().sentence(20));
        excursion.setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 300)));
        excursion.setDuration(duration);
        excursion.setDifficultyLevel(DifficultyLevel.values()[faker.random().nextInt(DifficultyLevel.values().length)]);
        excursion.setInclusions("Guide, Tickets, Transport");
        excursion.setRules("Follow guide instructions, bring water.");
        excursion.setNotRecommended("Pregnant women, people with heart conditions");
        excursion.setMaxParticipants(faker.number().numberBetween(5, 30));
        excursion.setCity(city);

        LocalDateTime now = LocalDateTime.now();
        excursion.setStartDate(now);
        switch (duration) {
            case "3 hours":
            case "5 hours":
            case "7 hours":
            case "10 hours":
            case "15 hours":
                excursion.setEndDate(now.plusHours(Integer.parseInt(duration.split(" ")[0])));
                break;
            case "1 day":
                excursion.setEndDate(now.plusDays(1));
                break;
            case "3 days":
                excursion.setEndDate(now.plusDays(3));
                break;
            case "1 week":
                excursion.setEndDate(now.plusWeeks(1));
                break;
        }
        return excursion;
    }


    private Excursion createExcursionForCountry(Country country, String duration) {
        Excursion excursion = new Excursion();
        excursion.setExcursionId(UUID.randomUUID());
        excursion.setTitle(faker.commerce().productName() + " in " + country.getName());
        excursion.setDescriptionExcursion(faker.lorem().sentence(20));
        excursion.setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 500)));
        excursion.setDuration(duration);
        excursion.setDifficultyLevel(DifficultyLevel.values()[faker.random().nextInt(DifficultyLevel.values().length)]);
        excursion.setInclusions("Guide, Tickets, Transport");
        excursion.setRules("Follow guide instructions, bring water.");
        excursion.setNotRecommended("Pregnant women, people with heart conditions");
        excursion.setMaxParticipants(faker.number().numberBetween(5, 30));
        excursion.setCountry(country);

        String region = country.getRegion();

        LocalDateTime now = LocalDateTime.now();
        excursion.setStartDate(now);
        switch (duration) {
            case "3 hours":
            case "5 hours":
            case "7 hours":
            case "10 hours":
            case "15 hours":
                excursion.setEndDate(now.plusHours(Integer.parseInt(duration.split(" ")[0])));
                break;
            case "1 day":
                excursion.setEndDate(now.plusDays(1));
                break;
            case "3 days":
                excursion.setEndDate(now.plusDays(3));
                break;
            case "1 week":
                excursion.setEndDate(now.plusWeeks(1));
                break;
        }
        return excursion;
    }


    private Excursion createExcursionForRegion(String region, String duration) {
        Excursion excursion = new Excursion();
        excursion.setExcursionId(UUID.randomUUID());
        excursion.setTitle(faker.commerce().productName() + " in regione " + region);
        excursion.setDescriptionExcursion(faker.lorem().sentence(20));
        excursion.setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 500)));
        excursion.setDuration(duration);
        excursion.setDifficultyLevel(DifficultyLevel.values()[faker.random().nextInt(DifficultyLevel.values().length)]);
        excursion.setInclusions("Guide, Tickets, Transport");
        excursion.setRules("Follow guide instructions, bring water.");
        excursion.setNotRecommended("Pregnant women, people with heart conditions");
        excursion.setMaxParticipants(faker.number().numberBetween(5, 30));

        LocalDateTime now = LocalDateTime.now();
        excursion.setStartDate(now);
        switch (duration) {
            case "3 hours":
            case "5 hours":
            case "7 hours":
            case "10 hours":
            case "15 hours":
                excursion.setEndDate(now.plusHours(Integer.parseInt(duration.split(" ")[0])));
                break;
            case "1 day":
                excursion.setEndDate(now.plusDays(1));
                break;
            case "3 days":
                excursion.setEndDate(now.plusDays(3));
                break;
            case "1 week":
                excursion.setEndDate(now.plusWeeks(1));
                break;
        }
        return excursion;
    }


    private void generateAvailabilityForExcursion(Excursion excursion) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime sixMonthsFromNow = currentDateTime.plusMonths(6);
        List<AvailabilityDate> availabilityDates = new ArrayList<>();

        while (currentDateTime.isBefore(sixMonthsFromNow)) {
            AvailabilityDate availabilityDate = new AvailabilityDate();
            availabilityDate.setExcursion(excursion);

            if (excursion.getDuration().matches("\\d+ hours")) {
                LocalDateTime availableDateTime = currentDateTime.withHour(faker.number().numberBetween(8, 20)).withMinute(0);
                if (availableDateTime.isAfter(excursion.getStartDate()) && availableDateTime.isBefore(excursion.getEndDate())) {
                    availabilityDate.setDateAvailable(availableDateTime);
                } else {
                    currentDateTime = currentDateTime.plusDays(1);
                    continue;
                }
                currentDateTime = currentDateTime.plusDays(1);
            } else {
                LocalDateTime availableDateTime = currentDateTime.toLocalDate().atStartOfDay();
                if (availableDateTime.isAfter(excursion.getStartDate()) && availableDateTime.isBefore(excursion.getEndDate())) {
                    availabilityDate.setDateAvailable(availableDateTime);
                } else {
                    currentDateTime = currentDateTime.plusDays(1);
                    continue;
                }

                currentDateTime = currentDateTime.plusDays(determineStep(excursion.getDuration()));
            }

            availabilityDate.setRemainingSeats(excursion.getMaxParticipants());
            availabilityDate.setIsBooked(false);
            availabilityDates.add(availabilityDate);
        }

        availabilityDateRepository.saveAll(availabilityDates);
    }

    private int determineStep(String duration) {
        switch (duration) {
            case "1 day":
                return 1;
            case "3 days":
                return 3;
            case "1 week":
                return 7;
            default:
                return 1;
        }
    }

    // DTO and pagination methods
    public List<ExcursionDTO> getExcursionsByCity(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Città non trovata"));
        return excursionRepository.findByCity(city).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExcursionDTO> findExcursionsByCityName(String cityName) {
        return excursionRepository.findByCity_Name(cityName).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExcursionDTO> findExcursionsByCountry(String countryName) {
        return cityRepository.findByCountry_Name(countryName).stream()
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
        return excursionRepository.findByCity_Name(cityName).stream()
                .filter(excursion -> (difficulty == null || excursion.getDifficultyLevel() == difficulty) &&
                        (minPrice == null || excursion.getPrice().compareTo(minPrice) >= 0) &&
                        (maxPrice == null || excursion.getPrice().compareTo(maxPrice) <= 0))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ExcursionDTO> getAllExcursionsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Excursion> excursions = excursionRepository.findAll(pageable);
        return excursions.map(this::mapToDTO);
    }

    public List<ExcursionDTO> findExcursionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Excursion> excursions = excursionRepository.findExcursionsByDateRange(startDate, endDate);
        return excursions.stream().map(this::mapToDTO).collect(Collectors.toList());
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
                excursion.getCity().getId(),  // City ID
                excursion.getCountry() != null ? excursion.getCountry().getId() : null,
                excursion.getStartDate(),
                excursion.getEndDate(),
                excursion.getMarkers()
        );
    }

    public long getTotalBookingsForExcursion(UUID excursionId) {
        return bookingRepository.countByExcursion_ExcursionId(excursionId);
    }

}
