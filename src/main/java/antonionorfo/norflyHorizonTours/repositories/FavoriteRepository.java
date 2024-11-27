package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUser_UserId(UUID userId);

    Optional<Favorite> findByUser_UserIdAndExcursion_ExcursionId(UUID userId, UUID excursionId);
}
