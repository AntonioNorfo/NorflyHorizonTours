package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.services.ExcursionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/excursions")
@RequiredArgsConstructor
public class ExcursionController {

    private static final Logger logger = LoggerFactory.getLogger(ExcursionController.class);

    private final ExcursionService excursionService;

    @GetMapping("/by-city")
    public ResponseEntity<List<ExcursionDTO>> getExcursionsByCity(@RequestParam UUID cityId) {
        logger.info("Fetching excursions for city ID: {}", cityId);

        try {
            List<ExcursionDTO> excursions = excursionService.getExcursionsByCity(cityId);
            if (excursions.isEmpty()) {
                logger.warn("No excursions found for city ID: {}", cityId);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(excursions);
        } catch (Exception e) {
            logger.error("Error retrieving excursions for city ID {}: {}", cityId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/city/{cityName}")
    public ResponseEntity<List<ExcursionDTO>> getExcursionsByCityName(@PathVariable String cityName) {
        logger.info("Fetching excursions for city: {}", cityName);

        List<ExcursionDTO> excursions = excursionService.findExcursionsByCityName(cityName);
        if (excursions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(excursions);
    }

    @GetMapping("/country/{countryName}")
    public ResponseEntity<List<ExcursionDTO>> getExcursionsByCountry(@PathVariable String countryName) {
        logger.info("Fetching excursions for country: {}", countryName);

        List<ExcursionDTO> excursions = excursionService.findExcursionsByCountry(countryName);
        if (excursions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(excursions);
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<ExcursionDTO>> getExcursionsByRegion(@PathVariable String region) {
        logger.info("Fetching excursions for region: {}", region);

        List<ExcursionDTO> excursions = excursionService.findExcursionsByRegion(region);
        if (excursions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(excursions);
    }

    @GetMapping("/filters")
    public ResponseEntity<List<ExcursionDTO>> getExcursionsByFilters(
            @RequestParam String cityName,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        logger.info("Fetching excursions by filters for city: {}, difficulty: {}, price range: {} - {}",
                cityName, difficulty, minPrice, maxPrice);

        List<ExcursionDTO> excursions = excursionService.findExcursionsByFilters(cityName, difficulty, minPrice, maxPrice);
        if (excursions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(excursions);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ExcursionDTO>> getAllExcursionsPaginated(@RequestParam int page, @RequestParam int size) {
        Page<ExcursionDTO> excursions = excursionService.getAllExcursionsPaginated(page, size);
        return ResponseEntity.ok(excursions);
    }

    @GetMapping("/excursions/{excursionId}/bookings")
    public ResponseEntity<Long> getTotalBookingsForExcursion(@PathVariable UUID excursionId) {
        long totalBookings = excursionService.getTotalBookingsForExcursion(excursionId);
        return ResponseEntity.ok(totalBookings);
    }


}
