package com.okanetransfer.repository;

import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
    List<User> findByAgencyId(Long agencyId);
}