package antonionorfo.norflyHorizonTours.repositories;

import antonionorfo.norflyHorizonTours.entities.City;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ExcursionRepository extends JpaRepository<Excursion, UUID> {
    List<Excursion> findByCity_Name(String cityName);

    @Query("SELECT e FROM Excursion e WHERE e.city.code = :cityCode")
    List<Excursion> findByCityCode(@Param("cityCode") String cityCode);

    List<Excursion> findByCity_Country_Region(String region);

    List<Excursion> findByCity_Country_Name(String countryName);

    List<Excursion> findByCity_Country_Code(String countryCode);

    long countByCity(City city);

    long countByCity_Country_Name(String countryName);

    boolean existsByTitleAndCity(String title, City city);

    @Query("SELECT COUNT(e) FROM Excursion e WHERE e.city.country.region = :region")
    long countByCity_Country_Region(@Param("region") String region);

    @Query("SELECT e FROM Excursion e WHERE e.city.country.name = :countryName AND e.title LIKE '%National%'")
    List<Excursion> findNationalExcursionsByCountry(@Param("countryName") String countryName);

    List<Excursion> findByCity(antonionorfo.norflyHorizonTours.entities.City city);

    @Query("SELECT e FROM Excursion e WHERE LOWER(e.city.name) = LOWER(:cityName)")
    List<Excursion> findByCityName(@Param("cityName") String cityName);

    @Query("SELECT e FROM Excursion e WHERE LOWER(e.city.name) = LOWER(:cityName) AND e.difficultyLevel = :difficulty")
    List<Excursion> findByCityNameAndDifficulty(@Param("cityName") String cityName, @Param("difficulty") String difficulty);

    @Query("SELECT e FROM Excursion e WHERE LOWER(e.city.name) = LOWER(:cityName) AND e.price BETWEEN :minPrice AND :maxPrice")
    List<Excursion> findByCityNameAndPriceRange(@Param("cityName") String cityName,
                                                @Param("minPrice") BigDecimal minPrice,
                                                @Param("maxPrice") BigDecimal maxPrice);

    Page<Excursion> findAll(Pageable pageable);
}

