package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.payloads.CountryDetailsDTO;
import antonionorfo.norflyHorizonTours.services.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    // Get all countries
    @GetMapping("/countries")
    public List<Map<String, String>> getAllCountries() {
        return cityService.fetchAllCountries();
    }

    // Get countries by region
    @GetMapping("/countries/region/{region}")
    public List<Map<String, String>> getCountriesByRegion(@PathVariable String region) {
        return cityService.fetchCountriesByRegion(region);
    }

    // Get details for a specific country
    @GetMapping("/countries/{code}")
    public CountryDetailsDTO getCountryDetails(@PathVariable String code) {
        return cityService.fetchCountryDetails(code);
    }

    // Get cities for a specific country
    @GetMapping("/countries/{countryCode}/cities")
    public List<String> getCitiesByCountryFromApi(@PathVariable String countryCode) {
        return cityService.fetchCitiesByCountry(countryCode);
    }

    // Get saved cities for a specific country
    @GetMapping("/countries/{country}/saved-cities")
    public List<City> getSavedCitiesByCountry(@PathVariable String country) {
        return cityService.getCitiesByCountry(country);
    }

    // Save a city
    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public City saveCity(@RequestBody Map<String, String> cityData) {
        String name = cityData.get("name");
        String country = cityData.get("country");
        String coordinates = cityData.getOrDefault("coordinates", "");
        return cityService.saveCity(name, country, coordinates);
    }
}
