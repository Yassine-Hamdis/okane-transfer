package com.okanetransfer.repository;

import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByRole(Role role);
    List<User> findAllByAgencyId(Long agencyId);
    List<User> findAllByAgencyIdAndRole(Long agencyId, Role role);
    List<User> findAllByActiveTrue();
    Optional<User> findByPhoneAndPhoneOtp(String phone, String otp);

    Optional<User> findByEmailToken(String token);
}