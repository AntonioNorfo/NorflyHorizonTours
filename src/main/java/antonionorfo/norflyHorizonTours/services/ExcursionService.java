package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.repositories.CityRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExcursionService {

    private static final Logger logger = LoggerFactory.getLogger(ExcursionService.class);

    private final ExcursionRepository excursionRepository;
    private final CityRepository cityRepository;

    private final Faker faker = new Faker();

    public void generateExcursionsForCities() {
        logger.info("Starting excursion generation for all cities...");

        List<City> cities = cityRepository.findAll();

        if (cities.isEmpty()) {
            logger.warn("No cities found in the database. No excursions will be generated.");
            return;
        }

        for (City city : cities) {
            try {
                logger.info("Generating excursions for city: {}", city.getName());
                List<Excursion> excursions = createExcursionsForCity(city);
                excursionRepository.saveAll(excursions);
                logger.info("Successfully saved {} excursions for city: {}", excursions.size(), city.getName());
            } catch (Exception e) {
                logger.error("Error while generating excursions for city: {}. Error: {}", city.getName(), e.getMessage());
            }
        }

        logger.info("Excursion generation process completed.");
    }

    private List<Excursion> createExcursionsForCity(City city) {
        logger.debug("Creating sample excursions for city: {}", city.getName());

        List<Excursion> excursions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Excursion excursion = new Excursion();
            excursion.setTitle(faker.commerce().productName() + " in " + city.getName());
            excursion.setDescription(faker.lorem().sentence(20));
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

        logger.debug("Created {} excursions for city: {}", excursions.size(), city.getName());
        return excursions;
    }

    public List<ExcursionDTO> getExcursionsByCity(UUID cityId) {
        logger.info("Fetching excursions for city ID: {}", cityId);

        Optional<City> optionalCity = cityRepository.findById(cityId);
        if (optionalCity.isEmpty()) {
            logger.error("City not found with ID: {}", cityId);
            throw new IllegalArgumentException("City not found with ID: " + cityId);
        }

        City city = optionalCity.get();
        logger.info("Found city: {}. Retrieving excursions...", city.getName());

        List<Excursion> excursions = excursionRepository.findAll();

        List<ExcursionDTO> excursionDTOs = excursions.stream()
                .filter(excursion -> excursion.getCity().equals(city))
                .map(excursion -> {
                    logger.debug("Mapping excursion to DTO: {}", excursion.getTitle());
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
                })
                .toList();

        if (excursionDTOs.isEmpty()) {
            logger.warn("No excursions found for city: {}", city.getName());
        } else {
            logger.info("Found {} excursions for city: {}", excursionDTOs.size(), city.getName());
        }

        return excursionDTOs;
    }
}
