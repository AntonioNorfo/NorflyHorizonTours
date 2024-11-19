package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.AdminPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminPostRepository extends JpaRepository<AdminPost, UUID> {
}
