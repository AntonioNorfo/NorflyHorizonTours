package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.AvailabilityDate;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityDateRepository extends JpaRepository<AvailabilityDate, UUID> {
    Optional<AvailabilityDate> findByExcursionAndDateAvailable(Excursion excursion, LocalDate dateAvailable);

    List<AvailabilityDate> findByExcursionAndIsBookedFalse(Excursion excursion);

    List<AvailabilityDate> findByExcursion(Excursion excursion);
    
    Optional<AvailabilityDate> findByExcursionAndDateAvailable(Excursion excursion, LocalDateTime dateTime);


    @Query("SELECT a FROM AvailabilityDate a WHERE a.excursion = :excursion AND a.dateAvailable BETWEEN :startDate AND :endDate")
    List<AvailabilityDate> findByExcursionAndDateRange(Excursion excursion, LocalDate startDate, LocalDate endDate);
}

