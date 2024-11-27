package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByExcursion(Excursion excursion);

    List<Review> findByUser_UserId(UUID userId);
}
