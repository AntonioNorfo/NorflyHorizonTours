package antonionorfo.norflyHorizonTours.tasks;

import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class BookingCleanupTask {

    private final BookingRepository bookingRepository;

    public BookingCleanupTask(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cancelUnpaidBookings() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        int deletedCount = bookingRepository.deleteByBookingDateBefore(cutoffTime);

        log.info("Deleted {} bookings older than 24 hours.", deletedCount);
    }

}
