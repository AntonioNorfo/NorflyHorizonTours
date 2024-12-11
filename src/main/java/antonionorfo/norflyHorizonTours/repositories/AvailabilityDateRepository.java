package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.AvailabilityDate;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityDateRepository extends JpaRepository<AvailabilityDate, UUID> {


    Optional<AvailabilityDate> findByExcursionAndDateAvailable(Excursion excursion, LocalDateTime dateAvailable);

    List<AvailabilityDate> findByExcursion(Excursion excursion);

    List<AvailabilityDate> findByExcursionAndIsBookedFalse(Excursion excursion);

    @Query("SELECT a FROM AvailabilityDate a WHERE a.excursion.excursionId = :excursionId AND a.isBooked = false AND a.remainingSeats > 0")
    List<AvailabilityDate> findAvailableDatesByExcursionId(UUID excursionId);

    @Query("SELECT a FROM AvailabilityDate a WHERE a.excursion.excursionId = :excursionId AND a.dateAvailable BETWEEN :startDate AND :endDate AND a.remainingSeats > 0 AND a.isBooked = false")
    List<AvailabilityDate> findAvailableDatesInRange(UUID excursionId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM AvailabilityDate a WHERE a.excursion.excursionId = :excursionId AND a.isBooked = false AND a.remainingSeats > 0")
    long countAvailableDatesByExcursionId(UUID excursionId);

    @Query("SELECT a FROM AvailabilityDate a WHERE a.excursion.excursionId = :excursionId")
    List<AvailabilityDate> findByExcursionId(UUID excursionId);

}
