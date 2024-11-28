package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.AvailabilityDate;
import antonionorfo.norflyHorizonTours.entities.Booking;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.BookingCreateDTO;
import antonionorfo.norflyHorizonTours.payloads.BookingDTO;
import antonionorfo.norflyHorizonTours.payloads.BookingUpdateDTO;
import antonionorfo.norflyHorizonTours.repositories.AvailabilityDateRepository;
import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ExcursionRepository excursionRepository;
    private final UserRepository userRepository;
    private final AvailabilityDateRepository availabilityDateRepository;

    public BookingDTO createBooking(BookingCreateDTO bookingCreateDTO) {
        User user = userRepository.findById(bookingCreateDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Excursion excursion = excursionRepository.findById(bookingCreateDTO.excursionId())
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        if (bookingRepository.existsByUserAndExcursion(user, excursion)) {
            throw new BadRequestException("User already has a booking for this excursion");
        }

        LocalDate startDate = bookingCreateDTO.startDate();
        AvailabilityDate availabilityDate = availabilityDateRepository
                .findByExcursionAndDateAvailable(excursion, startDate)
                .orElseThrow(() -> new BadRequestException("No available slots for this date"));

        if (availabilityDate.getRemainingSeats() <= 0) {
            throw new BadRequestException("No availability for this excursion on the selected date");
        }

        Booking booking = new Booking();
        booking.setBookingDate(LocalDate.now());
        booking.setStartDate(bookingCreateDTO.startDate());
        booking.setEndDate(bookingCreateDTO.endDate());
        booking.setStatusOfBooking("PENDING");
        booking.setUser(user);
        booking.setExcursion(excursion);

        Booking savedBooking = bookingRepository.save(booking);

        availabilityDate.setRemainingSeats(availabilityDate.getRemainingSeats() - 1);
        availabilityDateRepository.save(availabilityDate);

        return mapToDTO(savedBooking);
    }

    public List<BookingDTO> getUserBookings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUser(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getFutureBookings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUser(user).stream()
                .filter(booking -> booking.getStartDate().isAfter(LocalDate.now()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingHistory(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUser(user).stream()
                .filter(booking -> booking.getEndDate().isBefore(LocalDate.now()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BookingDTO getBookingDetails(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return mapToDTO(booking);
    }

    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"PENDING".equals(booking.getStatusOfBooking())) {
            throw new BadRequestException("Cannot cancel a non-pending booking");
        }

        AvailabilityDate availabilityDate = availabilityDateRepository
                .findByExcursionAndDateAvailable(booking.getExcursion(), booking.getStartDate())
                .orElseThrow(() -> new ResourceNotFoundException("Availability date not found"));

        availabilityDate.setRemainingSeats(availabilityDate.getRemainingSeats() + 1);
        availabilityDateRepository.save(availabilityDate);

        bookingRepository.delete(booking);
    }

    public BookingDTO updateBookingDates(UUID bookingId, BookingUpdateDTO updatedBooking) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (ChronoUnit.DAYS.between(LocalDate.now(), booking.getStartDate()) < 3) {
            throw new BadRequestException("Cannot update booking within 3 days of the excursion");
        }

        if (updatedBooking.startDate() != null) {
            booking.setStartDate(updatedBooking.startDate());
        }
        if (updatedBooking.endDate() != null) {
            booking.setEndDate(updatedBooking.endDate());
        }
        Booking savedBooking = bookingRepository.save(booking);
        return mapToDTO(savedBooking);
    }

    public int getExcursionAvailability(UUID excursionId, LocalDate date) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        return availabilityDateRepository.findByExcursionAndDateAvailable(excursion, date)
                .map(AvailabilityDate::getRemainingSeats)
                .orElse(0);
    }

    public List<LocalDate> getAvailableDatesForExcursion(UUID excursionId) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        return availabilityDateRepository.findByExcursionAndIsBookedFalse(excursion).stream()
                .map(availabilityDate -> availabilityDate.getDateAvailable().toLocalDate())
                .distinct()
                .collect(Collectors.toList());
    }


    private BookingDTO mapToDTO(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                booking.getUser().getUserId(),
                booking.getExcursion().getExcursionId(),
                booking.getBookingDate(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatusOfBooking()
        );
    }
}
