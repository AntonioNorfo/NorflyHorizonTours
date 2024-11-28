package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.LikeDislike;
import antonionorfo.norflyHorizonTours.entities.Review;
import antonionorfo.norflyHorizonTours.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LikeDislikeRepository extends JpaRepository<LikeDislike, UUID> {

    List<LikeDislike> findByReview(Review review);

    LikeDislike findByUserAndReview(User user, Review review);
}
