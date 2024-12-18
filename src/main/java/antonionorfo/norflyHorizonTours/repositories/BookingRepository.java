package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Booking;
import antonionorfo.norflyHorizonTours.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUser(User user);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate IS NOT NULL AND b.quantity > 0")
    List<Booking> findConfirmedBookingsByUser(@Param("user") User user);


    @Modifying
    @Transactional
    @Query("DELETE FROM Booking b WHERE b.bookingDate < :cutoffTime")
    int deleteByBookingDateBefore(@Param("cutoffTime") LocalDateTime cutoffTime);


    long countByExcursion_ExcursionId(UUID excursionId);


}
