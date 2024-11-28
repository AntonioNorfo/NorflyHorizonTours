package antonionorfo.norflyHorizonTours;

import antonionorfo.norflyHorizonTours.services.CityService;
import antonionorfo.norflyHorizonTours.services.CountryService;
import antonionorfo.norflyHorizonTours.services.ExcursionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CountryService countryService;
    private final CityService cityService;
    private final ExcursionService excursionService;

    @Override
    public void run(String... args) {

        try {
            countryService.populateCountries();
        } catch (Exception e) {
            System.err.println("Errore durante il popolamento dei paesi: " + e.getMessage());
        }

        try {
            cityService.populateCities();
        } catch (Exception e) {
            System.err.println("Errore durante il popolamento delle città: " + e.getMessage());
        }


        if (!excursionService.isExcursionsPopulated()) {
            try {

                excursionService.generateExcursionsForCities();
            } catch (Exception e) {
                System.err.println("Errore durante la generazione delle escursioni: " + e.getMessage());
            }
        } else {
            System.out.println("Il database è già popolato con le escursioni.");
        }
    }
}
