package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.CityDTO;
import antonionorfo.norflyHorizonTours.services.CityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
public class CityController {

    private static final Logger logger = LoggerFactory.getLogger(CityController.class);

    private final CityService cityService;

    @GetMapping("/db/{countryIdentifier}")
    public List<CityDTO> getCitiesByCountryFromDB(@PathVariable String countryIdentifier) {
        logger.info("Fetching cities from DB for country identifier: {}", countryIdentifier);
        return cityService.getCitiesByCountryFromDB(countryIdentifier);
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


}
