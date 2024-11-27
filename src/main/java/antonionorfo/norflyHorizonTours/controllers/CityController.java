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
        try {
            List<CityDTO> cities = cityService.getCitiesByCountryFromDB(countryIdentifier);
            if (cities.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cities);
        } catch (IllegalArgumentException e) {
            logger.error("Errore: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/geonames/{countryCode}")
    public List<CityDTO> getCitiesByCountryFromGeoNames(@PathVariable String countryCode) {
        logger.info("Fetching cities from GeoNames for country code: {}", countryCode);
        return cityService.getCitiesByCountryFromGeoNames(countryCode);
    }

    @GetMapping("/region/{region}/db")
    public List<CityDTO> getCitiesByRegionFromDB(@PathVariable String region) {
        logger.info("Fetching cities from DB for region: {}", region);
        return cityService.getCitiesByRegionFromDB(region);
    }

    @GetMapping("/region/{region}/geonames")
    public List<CityDTO> getCitiesByRegionFromGeoNames(@PathVariable String region) {
        logger.info("Fetching cities from GeoNames for region: {}", region);
        return cityService.getCitiesByRegionFromGeoNames(region);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CityDTO>> searchCities(@RequestParam String countryCode, @RequestParam String cityName) {
        return ResponseEntity.ok(cityService.searchCities(countryCode, cityName));
    }

}
