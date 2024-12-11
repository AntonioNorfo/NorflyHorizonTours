package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Cart;
import antonionorfo.norflyHorizonTours.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    void deleteByUserAndExcursion_ExcursionId(User user, UUID excursionId);

    Cart findByUserAndExcursion_ExcursionId(User user, UUID excursionId);

    Optional<Cart> findByUser(User user);

}
