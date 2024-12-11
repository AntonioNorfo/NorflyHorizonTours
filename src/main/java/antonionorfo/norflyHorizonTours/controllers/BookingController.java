package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.BookingCreateDTO;
import antonionorfo.norflyHorizonTours.payloads.BookingDTO;
import antonionorfo.norflyHorizonTours.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody @Valid BookingCreateDTO bookingCreateDTO) {
        logger.info("Creating booking for user: {} and excursion: {}", bookingCreateDTO.userId(), bookingCreateDTO.excursionId());
        try {
            BookingDTO booking = bookingService.createBooking(bookingCreateDTO);
            logger.info("Successfully created booking for user: {} and excursion: {}", bookingCreateDTO.userId(), bookingCreateDTO.excursionId());
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getUserBookings(@RequestParam UUID userId) {
        logger.info("Fetching bookings for user: {}", userId);
        try {
            List<BookingDTO> bookings = bookingService.getUserBookings(userId);
            if (bookings.isEmpty()) {
                logger.info("No bookings found for user: {}", userId);
                return ResponseEntity.noContent().build();
            }
            logger.info("Successfully fetched bookings for user: {}", userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            logger.error("Error fetching bookings for user: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID bookingId) {
        logger.info("Canceling booking ID: {}", bookingId);
        try {
            bookingService.cancelBooking(bookingId);
            logger.info("Successfully canceled booking ID: {}", bookingId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error canceling booking ID: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
