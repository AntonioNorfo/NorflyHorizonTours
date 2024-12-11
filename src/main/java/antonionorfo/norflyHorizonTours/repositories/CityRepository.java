package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {

    Optional<City> findByNameAndCountry(String name, Country country);

    boolean existsByNameAndCountry(String name, Country country);

    List<City> findByCountry(Country country);

    List<City> findByCountry_NameIgnoreCase(String countryName);

    List<City> findByCountry_RegionIgnoreCase(String region);

    List<City> findByName(String name);


    boolean existsByCode(String code);

    Optional<City> findByCode(String code);

    @Query("SELECT c FROM City c WHERE LOWER(c.country.name) = LOWER(:countryName)")
    List<City> findByCountry_Name(@Param("countryName") String countryName);

    @Query("SELECT c FROM City c WHERE LOWER(c.country.region) = LOWER(:region)")
    List<City> findByCountry_Region(@Param("region") String region);
}
