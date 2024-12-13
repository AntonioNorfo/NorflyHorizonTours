package antonionorfo.norflyHorizonTours;

import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.repositories.AvailabilityDateRepository;
import antonionorfo.norflyHorizonTours.repositories.CityRepository;
import antonionorfo.norflyHorizonTours.repositories.CountryRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.services.AvailabilityService;
import antonionorfo.norflyHorizonTours.services.CityService;
import antonionorfo.norflyHorizonTours.services.CountryService;
import antonionorfo.norflyHorizonTours.services.ExcursionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ExcursionRepository excursionRepository;
    private final AvailabilityDateRepository availabilityDateRepository;
    private final CountryService countryService;
    private final CityService cityService;
    private final ExcursionService excursionService;
    private final AvailabilityService availabilityService;

    @Override
    public void run(String... args) {
        initializeData();
    }

    @Transactional
    public void initializeData() {
        if (isDatabasePopulated()) {
            logger.info("Il database è già popolato. Nessuna inizializzazione necessaria.");
            return;
        }

        populateCountries();
        populateCities();
        populateExcursions();
        generateAvailabilityDatesForExcursions();
    }

    private boolean isDatabasePopulated() {
        boolean countriesExist = countryRepository.count() > 0;
        boolean citiesExist = cityRepository.count() > 0;
        boolean excursionsExist = excursionRepository.count() > 0;

        if (countriesExist && citiesExist && excursionsExist) {
            logger.info("Database già popolato: {} paesi, {} città, {} escursioni.",
                    countryRepository.count(),
                    cityRepository.count(),
                    excursionRepository.count());
            return true;
        }
        return false;
    }

    private void populateCountries() {
        if (countryRepository.count() > 0) {
            logger.info("I paesi sono già popolati. Skip del popolamento.");
            return;
        }

        try {
            logger.info("Popolamento dei paesi in corso...");
            countryService.populateCountries();
            logger.info("Popolamento dei paesi completato.");
        } catch (Exception e) {
            logger.error("Errore durante il popolamento dei paesi: {}", e.getMessage(), e);
        }
    }

    private void populateCities() {
        if (cityRepository.count() > 0) {
            logger.info("Le città sono già popolate. Skip del popolamento.");
            return;
        }

        try {
            logger.info("Popolamento delle città in corso...");
            cityService.populateCities();
            logger.info("Popolamento delle città completato.");
        } catch (Exception e) {
            logger.error("Errore durante il popolamento delle città: {}", e.getMessage(), e);
        }
    }

    private void populateExcursions() {
        if (excursionService.isExcursionsPopulated()) {
            logger.info("Le escursioni sono già popolate. Skip della generazione.");
            return;
        }

        try {
            logger.info("Generazione delle escursioni in corso...");
            excursionService.generateAllExcursions();
            logger.info("Generazione delle escursioni completata.");
        } catch (Exception e) {
            logger.error("Errore durante la generazione delle escursioni: {}", e.getMessage(), e);
        }
    }

    private void generateAvailabilityDatesForExcursions() {
        if (availabilityDateRepository.count() > 0) {
            logger.info("Le date di disponibilità sono già state generate. Skip della generazione.");
            return;
        }

        try {
            logger.info("Generazione delle date di disponibilità per le escursioni in corso...");
            List<Excursion> excursions = excursionRepository.findAll();

            excursions.forEach(excursion -> {
                try {
                    availabilityService.generateDefaultAvailabilityForExcursion(excursion);
                    logger.info("Disponibilità generata per l'escursione con ID: {}", excursion.getExcursionId());
                } catch (Exception e) {
                    logger.error("Errore nella generazione delle disponibilità per l'escursione con ID: {} - {}", excursion.getExcursionId(), e.getMessage());
                }
            });

            logger.info("Generazione delle date di disponibilità completata.");
        } catch (Exception e) {
            logger.error("Errore durante la generazione delle date di disponibilità: {}", e.getMessage(), e);
        }
    }
}
