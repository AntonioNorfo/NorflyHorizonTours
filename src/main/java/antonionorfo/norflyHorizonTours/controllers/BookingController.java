package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.payloads.BookingCreateDTO;
import antonionorfo.norflyHorizonTours.payloads.BookingDTO;
import antonionorfo.norflyHorizonTours.payloads.BookingUpdateDTO;
import antonionorfo.norflyHorizonTours.services.AvailabilityService;
import antonionorfo.norflyHorizonTours.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody @Valid BookingCreateDTO bookingCreateDTO) {
        logger.info("Creating booking for user: {} and excursion: {}", bookingCreateDTO.userId(), bookingCreateDTO.excursionId());
        BookingDTO booking = bookingService.createBooking(bookingCreateDTO);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/book")
    public ResponseEntity<String> bookSeats(@RequestParam UUID excursionId, @RequestParam LocalDate date, @RequestParam int numSeats) {
        try {
            availabilityService.bookSeats(excursionId, date, numSeats);
            return ResponseEntity.ok("Booking successful!");
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getUserBookings(@RequestParam UUID userId) {
        logger.info("Fetching bookings for user: {}", userId);
        List<BookingDTO> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/future")
    public ResponseEntity<List<BookingDTO>> getFutureBookings(@RequestParam UUID userId) {
        logger.info("Fetching future bookings for user: {}", userId);
        List<BookingDTO> bookings = bookingService.getFutureBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/history")
    public ResponseEntity<List<BookingDTO>> getBookingHistory(@RequestParam UUID userId) {
        logger.info("Fetching booking history for user: {}", userId);
        List<BookingDTO> bookings = bookingService.getBookingHistory(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> getBookingDetails(@PathVariable UUID bookingId) {
        logger.info("Fetching details for booking ID: {}", bookingId);
        BookingDTO bookingDetails = bookingService.getBookingDetails(bookingId);
        return ResponseEntity.ok(bookingDetails);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID bookingId) {
        logger.info("Canceling booking ID: {}", bookingId);
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> updateBookingDates(
            @PathVariable UUID bookingId,
            @RequestBody @Valid BookingUpdateDTO updatedBooking) {
        logger.info("Updating booking dates for ID: {}", bookingId);
        BookingDTO updated = bookingService.updateBookingDates(bookingId, updatedBooking);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/availability/{excursionId}")
    public ResponseEntity<Integer> checkExcursionAvailability(
            @PathVariable UUID excursionId,
            @RequestParam LocalDate date) {
        logger.info("Checking availability for excursion ID: {} on date: {}", excursionId, date);
        int availableSlots = availabilityService.getExcursionAvailability(excursionId, date);
        return ResponseEntity.ok(availableSlots);
    }

    @GetMapping("/availability/dates/{excursionId}")
    public ResponseEntity<List<LocalDate>> getAvailableDates(@PathVariable UUID excursionId) {
        logger.info("Fetching available dates for excursion ID: {}", excursionId);
        List<LocalDate> availableDates = availabilityService.getAvailableDatesForExcursion(excursionId);
        return ResponseEntity.ok(availableDates);
    }
}
