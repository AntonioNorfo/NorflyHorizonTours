package antonionorfo.norflyHorizonTours;

import antonionorfo.norflyHorizonTours.services.CityService;
import antonionorfo.norflyHorizonTours.services.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CountryService countryService;
    private final CityService cityService;

    @Override
    public void run(String... args) {
        countryService.populateCountries();
        
        cityService.populateCities();

        cityService.generateExcursionsForCities();
    }
}
