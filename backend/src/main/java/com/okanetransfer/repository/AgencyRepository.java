package com.okanetransfer.repository;

import com.okanetransfer.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    List<Agency> findByCountryId(Long countryId);
    Optional<Agency> findByManagerId(Long managerId);
}