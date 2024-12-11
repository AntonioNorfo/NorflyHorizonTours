package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.CountryDTO;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO;
import antonionorfo.norflyHorizonTours.services.CountryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

    private static final Logger logger = LoggerFactory.getLogger(CountryController.class);
    private final CountryService countryService;

    @GetMapping("/db")
    public ResponseEntity<List<CountryDTO>> getAllCountriesFromDB() {
        logger.info("Fetching all countries from DB");
        try {
            List<CountryDTO> countries = countryService.getAllCountriesFromDB();
            if (countries.isEmpty()) {
                logger.warn("No countries found in the database.");
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            logger.error("Error fetching all countries from DB: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/geonames")
    public ResponseEntity<List<CountryDTO>> getAllCountriesFromGeoNames() {
        logger.info("Fetching all countries from GeoNames API");
        try {
            List<CountryDTO> countries = countryService.getAllCountriesFromGeoNames();
            if (countries.isEmpty()) {
                logger.warn("No countries found from GeoNames API.");
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            logger.error("Error fetching all countries from GeoNames API: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/db/{countryIdentifier}")
    public ResponseEntity<CountryDTO> getCountryDetailsFromDB(@PathVariable String countryIdentifier) {
        logger.info("Fetching country details from DB for identifier: {}", countryIdentifier);
        try {
            CountryDTO country = countryService.getCountryDetailsFromDB(countryIdentifier);
            return ResponseEntity.ok(country);
        } catch (IllegalArgumentException e) {
            logger.warn("Country not found for identifier: {}", countryIdentifier);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching country details from DB: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/geonames/{identifier}")
    public ResponseEntity<CountryDetailsDTO> getCountryDetailsFromGeoNames(@PathVariable String identifier) {
        logger.info("Fetching country details from GeoNames for identifier: {}", identifier);
        try {
            CountryDetailsDTO countryDetails;

            if (identifier.length() == 2) {
                countryDetails = countryService.getCountryDetailsFromGeoNames(identifier, null);
            } else {
                // Altrimenti, usalo come nome del paese
                countryDetails = countryService.getCountryDetailsFromGeoNames(null, identifier);
            }

            if (countryDetails == null) {
                logger.warn("Country not found for identifier: {}", identifier);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(countryDetails);
        } catch (IllegalArgumentException e) {
            logger.warn("Country not found for identifier: {}", identifier);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching country details from GeoNames: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/region/{region}/geonames")
    public ResponseEntity<List<CountryDTO>> getCountriesByRegionFromGeoNames(@PathVariable String region) {
        logger.info("Fetching countries by region from GeoNames for region: {}", region);
        try {
            List<CountryDTO> countries = countryService.getCountriesByRegionFromGeoNames(region);
            if (countries.isEmpty()) {
                logger.warn("No countries found in region: {}", region);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            logger.error("Error fetching countries by region from GeoNames: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/region/{region}/db")
    public ResponseEntity<List<CountryDTO>> getCountriesByRegionFromDB(@PathVariable String region) {
        logger.info("Fetching countries by region from DB for region: {}", region);
        try {
            List<CountryDTO> countries = countryService.getCountriesByRegionFromDB(region);
            if (countries.isEmpty()) {
                logger.warn("No countries found in region: {}", region);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            logger.error("Error fetching countries by region from DB: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<CountryDTO>> searchCountries(@RequestParam String query) {
        logger.info("Searching countries with query: {}", query);
        try {
            List<CountryDTO> countries = countryService.searchCountries(query);
            if (countries.isEmpty()) {
                logger.warn("No countries found for query: {}", query);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            logger.error("Error searching countries: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
