package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Booking;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUser(User user);

    List<Booking> findByUserAndStatusOfBooking(User user, String statusOfBooking);

    List<Booking> findByUserAndStartDateAfter(User user, LocalDate startDate);

    boolean existsByUserAndExcursion(User user, Excursion excursion);

    boolean existsByBookingIdAndBookingDateAfter(UUID bookingId, LocalDateTime limitDate);

    long countByExcursion(Excursion excursion);

    long countByExcursion_ExcursionId(UUID excursionId);

    List<Booking> findByExcursionAndStartDate(Excursion excursion, LocalDate startDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM Booking b WHERE b.statusOfBooking = :status AND b.bookingDate < :cutoffTime")
    int deleteByStatusAndBookingDateBefore(@Param("status") String status, @Param("cutoffTime") LocalDateTime cutoffTime);

}
