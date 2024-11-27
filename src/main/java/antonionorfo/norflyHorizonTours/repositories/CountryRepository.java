package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {
    boolean existsByName(String name);

    List<Country> findByRegionIgnoreCase(String region);

    Optional<Country> findByNameIgnoreCase(String name);

    Optional<Country> findByCodeIgnoreCase(String code);

    Optional<Country> findByName(String name);

    Optional<Country> findByCode(String code);

    @Query("SELECT c FROM Country c WHERE c.region = :region")
    List<Country> findByRegion(@Param("region") String region);
}
