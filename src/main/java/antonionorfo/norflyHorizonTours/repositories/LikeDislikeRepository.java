package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.LikeDislike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikeDislikeRepository extends JpaRepository<LikeDislike, UUID> {
}
