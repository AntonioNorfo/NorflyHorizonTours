package antonionorfo.norflyHorizonTours.controllers;


import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.services.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);
    private final AvailabilityService availabilityService;
    @Autowired
    private ExcursionRepository excursionRepository;

    @GetMapping("/excursion/{excursionId}/dates")
    public ResponseEntity<List<LocalDateTime>> getAvailableDatesForExcursion(@PathVariable UUID excursionId) {
        logger.info("Fetching available dates for excursion: {}", excursionId);
        try {
            List<LocalDateTime> availableDates = availabilityService.getAvailableDates(excursionId);
            logger.info("Available dates for excursion {}: {}", excursionId, availableDates);
            return ResponseEntity.ok(availableDates);
        } catch (Exception e) {
            logger.error("Error fetching available dates for excursion {}: {}", excursionId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/excursion/{excursionId}/availability")
    public ResponseEntity<Integer> getAvailableSeats(
            @PathVariable UUID excursionId,
            @RequestParam LocalDateTime dateTime) {
        logger.info("Fetching available seats for excursion {} on {}", excursionId, dateTime);
        try {
            int availableSeats = availabilityService.countAvailableSeats(excursionId, dateTime);
            logger.info("Available seats for excursion {} on {}: {}", excursionId, dateTime, availableSeats);
            return ResponseEntity.ok(availableSeats);
        } catch (Exception e) {
            logger.error("Error fetching available seats for excursion {}: {}", excursionId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/excursion/{excursionId}/update")
    public ResponseEntity<String> updateAvailability(
            @PathVariable UUID excursionId,
            @RequestParam LocalDateTime dateTime,
            @RequestParam int seatsBooked) {
        logger.info("Updating availability for excursion {} on {} with {} seats booked", excursionId, dateTime, seatsBooked);
        try {
            availabilityService.updateAvailability(excursionId, dateTime, seatsBooked);
            logger.info("Successfully updated availability for excursion {} on {}", excursionId, dateTime);
            return ResponseEntity.ok("Availability successfully updated.");
        } catch (Exception e) {
            logger.error("Error updating availability for excursion {}: {}", excursionId, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/excursion/{excursionId}/generate")
    public ResponseEntity<String> generateAvailability(@PathVariable UUID excursionId) {
        logger.info("Generating availability dates for excursion: {}", excursionId);
        try {
            availabilityService.generateAvailabilityForExcursion(excursionId);
            logger.info("Successfully generated availability dates for excursion {}", excursionId);
            return ResponseEntity.ok("Availability dates successfully generated.");
        } catch (Exception e) {
            logger.error("Error generating availability dates for excursion {}: {}", excursionId, e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/generate-for-all")
    public ResponseEntity<String> generateAvailabilityForAllExcursions() {
        logger.info("Generazione delle disponibilità per tutte le escursioni...");
        int pageSize = 100; // Numero di escursioni da elaborare per pagina
        int pageNumber = 0;
        long totalProcessed = 0;

        while (true) {
            Page<Excursion> excursionPage = excursionRepository.findAll(PageRequest.of(pageNumber, pageSize));
            if (excursionPage.isEmpty()) {
                break;
            }

            excursionPage.forEach(availabilityService::generateDefaultAvailabilityForExcursion);
            totalProcessed += excursionPage.getNumberOfElements();
            pageNumber++;
        }

        logger.info("Completata la generazione delle disponibilità per tutte le escursioni. Totale escursioni elaborate: {}", totalProcessed);
        return ResponseEntity.ok("Disponibilità generate per tutte le escursioni.");
    }


}
