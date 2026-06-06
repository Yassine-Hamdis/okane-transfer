package com.okanetransfer.repository;

import com.okanetransfer.entity.Corridor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorridorRepository extends JpaRepository<Corridor, Long> {
    List<Corridor> findAllByActiveTrue();
    Optional<Corridor> findBySourceCountryIdAndDestinationCountryId(
            Long sourceCountryId, Long destinationCountryId);
    List<Corridor> findAllBySourceCountryId(Long sourceCountryId);
    List<Corridor> findAllByDestinationCountryId(Long destinationCountryId);

    boolean existsBySourceCountryIdAndDestinationCountryId(
            Long sourceId,
            Long destinationId
    );
    List<Corridor> findBySourceCountryIdOrDestinationCountryId(
            Long sourceCountryId,
            Long destinationCountryId);

    @Query("""
    SELECT c
    FROM Corridor c
    WHERE (:sourceCountryId IS NULL OR c.sourceCountry.id = :sourceCountryId)
      AND (:destinationCountryId IS NULL OR c.destinationCountry.id = :destinationCountryId)
      AND (:active IS NULL OR c.active = :active)
""")
    Page<Corridor> search(
            @Param("sourceCountryId") Long sourceCountryId,
            @Param("destinationCountryId") Long destinationCountryId,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Modifying
    @Query("""
    UPDATE Corridor c
    SET c.active = false
    WHERE c.sourceCountry.defaultCurrency.id = :currencyId
       OR c.destinationCountry.defaultCurrency.id = :currencyId
    """)
    void disableByCurrency(@Param("currencyId") Long currencyId);

    @Modifying
    @Query("""
    UPDATE Corridor c
    SET c.active = false
    WHERE c.sourceCountry.id = :countryId
       OR c.destinationCountry.id = :countryId
    """)
    void disableByCountry(@Param("countryId") Long countryId);

    @Query("""
    SELECT c FROM Corridor c
    JOIN FETCH c.sourceCountry sc
    JOIN FETCH c.destinationCountry dc
    WHERE sc.id = :sourceCountryId
    AND dc.id = :destinationCountryId
""")
    Optional<Corridor> findExactCorridor(
            @Param("sourceCountryId")Long sourceCountryId,
            @Param("destinationCountryId")Long destinationCountryId
    );

}