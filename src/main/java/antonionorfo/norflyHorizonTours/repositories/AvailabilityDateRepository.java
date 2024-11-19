package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.AvailabilityDate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AvailabilityDateRepository extends JpaRepository<AvailabilityDate, UUID> {
}
