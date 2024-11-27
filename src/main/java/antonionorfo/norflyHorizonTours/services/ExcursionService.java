package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.repositories.AvailabilityDateRepository;
import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import antonionorfo.norflyHorizonTours.repositories.CityRepository;
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

    private static final int EXCURSIONS_PER_CITY = 10;
    private static final int EXCURSIONS_PER_REGION = 15;
    private static final int EXCURSIONS_PER_COUNTRY = 15;

    private final ExcursionRepository excursionRepository;
    private final CityRepository cityRepository;
    private final AvailabilityDateRepository availabilityDateRepository;
    private final Faker faker = new Faker();
    private final AvailabilityService availabilityService;
    private final BookingRepository bookingRepository;

    public boolean isExcursionsPopulated() {
        long totalExcursions = excursionRepository.count();
        long requiredExcursions = cityRepository.count() * EXCURSIONS_PER_CITY;

        if (totalExcursions >= requiredExcursions) {
            logger.info("Il DB è già popolato con il numero richiesto di escursioni.");
            return true;
        }
        return false;
    }

    public void generateExcursionsForCities() {
        if (isExcursionsPopulated()) {
            return;
        }

        logger.info("Inizio la generazione delle escursioni per tutte le città...");
        List<City> cities = cityRepository.findAll();

        cities.stream()
                .collect(Collectors.groupingBy(city -> city.getCountry().getRegion()))
                .forEach((region, regionCities) -> {
                    try {
                        long existingExcursionsInRegion = excursionRepository.countByCity_Country_Region(region);
                        if (existingExcursionsInRegion >= EXCURSIONS_PER_REGION) {
                            logger.info("La regione '{}' ha già {} escursioni. Salto...", region, existingExcursionsInRegion);
                            return;
                        }

                        long existingExcursionsInCountry = excursionRepository.countByCity_Country_Name(regionCities.get(0).getCountry().getName());
                        if (existingExcursionsInCountry >= EXCURSIONS_PER_COUNTRY) {
                            logger.info("Il paese '{}' ha già {} escursioni. Salto...", regionCities.get(0).getCountry().getName(), existingExcursionsInCountry);
                            return;
                        }

                        final int[] remainingExcursionsForRegion = {EXCURSIONS_PER_REGION - (int) existingExcursionsInRegion};
                        final int[] remainingExcursionsForCountry = {EXCURSIONS_PER_COUNTRY - (int) existingExcursionsInCountry};

                        regionCities.forEach(city -> {

                            long existingExcursionsInCity = excursionRepository.countByCity(city);
                            if (existingExcursionsInCity >= EXCURSIONS_PER_CITY) {
                                return;
                            }

                            int remainingExcursionsForCity = EXCURSIONS_PER_CITY - (int) existingExcursionsInCity;
                            List<Excursion> excursions = createExcursionsForCity(city, Math.min(remainingExcursionsForCity, remainingExcursionsForRegion[0]));
                            for (Excursion excursion : excursions) {
                                if (excursionRepository.existsByTitleAndCity(excursion.getTitle(), city)) {
                                    logger.info("Escursione '{}' già esiste nella città: {}", excursion.getTitle(), city.getName());
                                    continue;
                                }
                                excursionRepository.save(excursion);
                                logger.info("Escursione '{}' salvata nella città: {}", excursion.getTitle(), city.getName());

                                availabilityService.generateDefaultAvailability(excursion);

                                remainingExcursionsForRegion[0]--;
                                remainingExcursionsForCountry[0]--;
                                remainingExcursionsForCity--;
                                if (remainingExcursionsForRegion[0] <= 0 || remainingExcursionsForCountry[0] <= 0 || remainingExcursionsForCity <= 0) {
                                    break;
                                }
                            }
                        });
                    } catch (Exception e) {
                        logger.error("Errore nella generazione delle escursioni per la regione: {} - {}", region, e.getMessage());
                    }
                });
    }

    public long getTotalBookingsForExcursion(UUID excursionId) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found with ID: " + excursionId));

        return bookingRepository.countByExcursion(excursion);
    }

    private List<Excursion> createExcursionsForCity(City city, int numberOfExcursions) {
        List<Excursion> excursions = new ArrayList<>();
        for (int i = 0; i < numberOfExcursions; i++) {
            Excursion excursion = new Excursion();
            excursion.setExcursionId(UUID.randomUUID());
            excursion.setTitle(faker.commerce().productName() + " in " + city.getName());
            excursion.setDescriptionExcursion(faker.lorem().paragraph());
            excursion.setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 500)));
            excursion.setDuration(faker.number().numberBetween(2, 8) + " ore");
            excursion.setDifficultyLevel(DifficultyLevel.values()[faker.random().nextInt(DifficultyLevel.values().length)]); // Random difficulty level
            excursion.setInclusions(faker.lorem().sentence());
            excursion.setRules(faker.lorem().sentence());
            excursion.setNotRecommended(faker.lorem().sentence());
            excursion.setMaxParticipants(faker.number().numberBetween(10, 50));
            excursion.setCity(city);
            excursions.add(excursion);
        }
        return excursions;
    }

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
                excursion.getNotRecommended(),
                excursion.getMaxParticipants(),
                excursion.getCity().getId()
        );
    }
}

