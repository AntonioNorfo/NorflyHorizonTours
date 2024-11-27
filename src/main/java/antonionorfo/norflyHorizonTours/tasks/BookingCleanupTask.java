package antonionorfo.norflyHorizonTours.tasks;

import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class BookingCleanupTask {

    private final BookingRepository bookingRepository;

    public BookingCleanupTask(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cancelUnpaidBookings() {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.findAll().stream()
                .filter(booking -> "PENDING".equals(booking.getStatusOfBooking()))
                .filter(booking -> ChronoUnit.HOURS.between(booking.getBookingDate().atStartOfDay(), now) > 24)
                .forEach(booking -> {
                    bookingRepository.delete(booking);
                    System.out.println("Deleted unpaid booking with ID: " + booking.getBookingId());
                });
    }
}
