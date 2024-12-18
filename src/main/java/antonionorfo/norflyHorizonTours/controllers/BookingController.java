package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.BookingDTO;
import antonionorfo.norflyHorizonTours.services.BookingService;
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
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getUserBookings(@RequestParam UUID userId) {
        logger.info("Fetching confirmed bookings for user: {}", userId);
        try {
            List<BookingDTO> bookings = bookingService.getConfirmedBookingsByUser(userId);
            if (bookings.isEmpty()) {
                logger.info("No confirmed bookings found for user: {}", userId);
                return ResponseEntity.noContent().build();
            }
            logger.info("Successfully fetched confirmed bookings for user: {}", userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            logger.error("Error fetching confirmed bookings for user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
