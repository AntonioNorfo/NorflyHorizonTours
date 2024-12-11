package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Booking;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.BookingCreateDTO;
import antonionorfo.norflyHorizonTours.payloads.BookingDTO;
import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ExcursionRepository excursionRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;

    public BookingDTO createBooking(BookingCreateDTO bookingCreateDTO) {
        User user = userRepository.findById(bookingCreateDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Excursion excursion = excursionRepository.findById(bookingCreateDTO.excursionId())
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        if (bookingRepository.existsByUserAndExcursion(user, excursion)) {
            throw new BadRequestException("User already has a booking for this excursion");
        }

        LocalDateTime bookingDateTime = bookingCreateDTO.bookingDateTime();
        int numSeats = bookingCreateDTO.numSeats();

        int availableSeats = availabilityService.countAvailableSeats(excursion.getExcursionId(), bookingDateTime);
        if (availableSeats < numSeats) {
            throw new BadRequestException("Not enough seats available for this excursion on the selected date");
        }

        availabilityService.updateAvailability(excursion.getExcursionId(), bookingDateTime, numSeats);

        Booking booking = new Booking();
        booking.setBookingDate(LocalDateTime.now());
        booking.setStartDate(bookingDateTime);
        booking.setEndDate(bookingCreateDTO.endDateTime());
        booking.setNumSeats(numSeats);
        booking.setStatusOfBooking("CONFIRMED");
        booking.setUser(user);
        booking.setExcursion(excursion);

        Booking savedBooking = bookingRepository.save(booking);
        return mapToDTO(savedBooking);
    }

    public List<BookingDTO> getUserBookings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUser(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"CONFIRMED".equals(booking.getStatusOfBooking())) {
            throw new BadRequestException("Cannot cancel a non-confirmed booking");
        }

        availabilityService.updateAvailability(booking.getExcursion().getExcursionId(),
                booking.getStartDate(), -booking.getNumSeats());

        bookingRepository.delete(booking);
    }

    private BookingDTO mapToDTO(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                booking.getUser().getUserId(),
                booking.getExcursion().getExcursionId(),
                booking.getBookingDate(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getNumSeats(),
                booking.getStatusOfBooking()
        );
    }
}
