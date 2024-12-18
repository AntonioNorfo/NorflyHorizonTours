package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Booking;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.BookingDTO;
import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public List<BookingDTO> getConfirmedBookingsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Booking> confirmedBookings = bookingRepository.findConfirmedBookingsByUser(user);

        return confirmedBookings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BookingDTO mapToDTO(Booking booking) {
        Excursion excursion = booking.getExcursion();
        return new BookingDTO(
                booking.getBookingId(),
                excursion.getTitle(),
                excursion.getDescriptionExcursion(),
                booking.getBookingDate(),
                excursion.getPrice(),
                excursion.getDuration(),
                booking.getQuantity(),
                booking.getTotalPrice()
        );
    }


}
