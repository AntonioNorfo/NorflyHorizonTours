package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Excursion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExcursionRepository extends JpaRepository<Excursion, UUID> {
}
