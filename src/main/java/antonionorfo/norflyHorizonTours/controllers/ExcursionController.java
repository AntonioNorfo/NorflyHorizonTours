package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.services.ExcursionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        logger.info("Endpoint hit: /excursions/by-city with cityId: {}", cityId);

        if (cityId == null) {
            logger.error("City ID is missing in request.");
            return ResponseEntity.badRequest().build();
        }

        try {
            List<ExcursionDTO> excursions = excursionService.getExcursionsByCity(cityId);
            if (excursions.isEmpty()) {
                logger.warn("No excursions found for cityId: {}", cityId);
                return ResponseEntity.noContent().build();
            }

            logger.info("Found {} excursions for cityId: {}", excursions.size(), cityId);
            return ResponseEntity.ok(excursions);
        } catch (Exception e) {
            logger.error("Error retrieving excursions for cityId: {}. Error: {}", cityId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
