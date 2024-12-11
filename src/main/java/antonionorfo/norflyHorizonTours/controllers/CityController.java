package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.CityDTO;
import antonionorfo.norflyHorizonTours.services.CityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
public class CityController {

    private static final Logger logger = LoggerFactory.getLogger(CityController.class);
    private final CityService cityService;

    @GetMapping("/db/{countryIdentifier}")
    public ResponseEntity<List<CityDTO>> getCitiesByCountryFromDB(@PathVariable String countryIdentifier) {
        logger.info("Fetching cities from DB for country identifier: {}", countryIdentifier);
        try {
            List<CityDTO> cities = cityService.getCitiesByCountryFromDB(countryIdentifier);
            if (cities.isEmpty()) {
                logger.warn("No cities found for country identifier: {}", countryIdentifier);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cities);
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching cities: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/geonames/{countryCode}/{countryName}")
    public ResponseEntity<List<CityDTO>> getCitiesByCountryFromGeoNames(
            @PathVariable String countryCode,
            @PathVariable String countryName) {
        logger.info("Fetching cities from GeoNames for country code: {} or country name: {}", countryCode, countryName);

        try {
            List<CityDTO> cities = cityService.getCitiesByCountryFromGeoNames(countryCode, countryName);

            if (cities.isEmpty()) {
                logger.warn("No cities found from GeoNames for country code: {} or country name: {}", countryCode, countryName);
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            logger.error("Error fetching cities from GeoNames for country code: {} or country name: {}: {}", countryCode, countryName, e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/region/{region}/db")
    public ResponseEntity<List<CityDTO>> getCitiesByRegionFromDB(@PathVariable String region) {
        logger.info("Fetching cities from DB for region: {}", region);
        try {
            List<CityDTO> cities = cityService.getCitiesByRegionFromDB(region);
            if (cities.isEmpty()) {
                logger.warn("No cities found for region: {}", region);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            logger.error("Error fetching cities by region: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/region/{region}/geonames")
    public ResponseEntity<List<CityDTO>> getCitiesByRegionFromGeoNames(@PathVariable String region) {
        logger.info("Fetching cities from GeoNames for region: {}", region);
        try {
            List<CityDTO> cities = cityService.getCitiesByRegionFromGeoNames(region);
            if (cities.isEmpty()) {
                logger.warn("No cities found from GeoNames for region: {}", region);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            logger.error("Error fetching cities from GeoNames for region: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<CityDTO>> searchCities(
            @RequestParam String countryCode,
            @RequestParam String cityName
    ) {
        logger.info("Searching for cities in countryCode: {}, with cityName: {}", countryCode, cityName);
        try {
            List<CityDTO> cities = cityService.searchCities(countryCode, cityName);
            if (cities.isEmpty()) {
                logger.warn("No cities found for search parameters: countryCode={}, cityName={}", countryCode, cityName);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            logger.error("Error searching cities: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
