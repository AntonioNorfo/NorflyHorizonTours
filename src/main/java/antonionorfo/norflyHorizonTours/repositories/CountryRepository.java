package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {

    boolean existsByName(String name);

    List<Country> findByRegionIgnoreCase(String region);

    Optional<Country> findByNameIgnoreCase(String name);

    Optional<Country> findByCodeIgnoreCase(String code);

    Optional<Country> findByName(String name);

    Optional<Country> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT c FROM Country c WHERE LOWER(c.region) = LOWER(:region)")
    List<Country> findByRegion(@Param("region") String region);

    @Query("SELECT DISTINCT c.region FROM Country c WHERE c.region IS NOT NULL")
    List<String> findDistinctRegions();

}
