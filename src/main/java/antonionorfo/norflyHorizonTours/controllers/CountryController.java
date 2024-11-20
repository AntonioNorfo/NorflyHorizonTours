package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.CountryDTO;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO;
import antonionorfo.norflyHorizonTours.services.CountryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

    private static final Logger logger = LoggerFactory.getLogger(CountryController.class);

    private final CountryService countryService;

    @GetMapping("/db")
    public List<CountryDTO> getAllCountriesFromDB() {
        logger.info("Fetching all countries from DB");
        return countryService.getAllCountriesFromDB();
    }

    @GetMapping("/geonames")
    public List<CountryDTO> getAllCountriesFromGeoNames() {
        logger.info("Fetching all countries from GeoNames");
        return countryService.getAllCountriesFromGeoNames();
    }

    @GetMapping("/db/{countryIdentifier}")
    public CountryDTO getCountryDetailsFromDB(@PathVariable String countryIdentifier) {
        logger.info("Fetching country details from DB for identifier: {}", countryIdentifier);
        return countryService.getCountryDetailsFromDB(countryIdentifier);
    }


    @GetMapping("/geonames/{countryCode}")
    public CountryDetailsDTO getCountryDetailsFromGeoNames(@PathVariable String countryCode) {
        logger.info("Fetching country details from GeoNames for code: {}", countryCode);
        return countryService.getCountryDetailsFromGeoNames(countryCode);
    }

    @GetMapping("/region/{region}/geonames")
    public List<CountryDTO> getCountriesByRegionFromGeoNames(@PathVariable String region) {
        logger.info("Fetching countries by region from GeoNames: {}", region);
        return countryService.getCountriesByRegionFromGeoNames(region);
    }

    @GetMapping("/region/{region}/db")
    public List<CountryDTO> getCountriesByRegionFromDB(@PathVariable String region) {
        logger.info("Fetching countries by region from DB: {}", region);
        return countryService.getCountriesByRegionFromDB(region);
    }
}
