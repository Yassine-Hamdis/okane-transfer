package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateAgencyRequest;
import com.okanetransfer.dto.request.UpdateAgencyRequest;
import com.okanetransfer.dto.response.AgencyResponse;
import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.CashRegister;
import com.okanetransfer.entity.Country;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.CashRegisterRepository;
import com.okanetransfer.repository.CountryRepository;
import com.okanetransfer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgencyService {

    @Autowired private AgencyRepository      agencyRepository;
    @Autowired private CountryRepository     countryRepository;
    @Autowired private UserRepository        userRepository;
    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private AuditService          auditService;

    // ─────────────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AgencyResponse> getAllAgencies() {
        return agencyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgencyResponse> getActiveAgencies() {
        return agencyRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgencyResponse getAgencyById(Long id) {
        return toResponse(findAgency(id));
    }

    @Transactional(readOnly = true)
    public List<AgencyResponse> getAgenciesByCountry(Long countryId) {
        return agencyRepository.findAllByCountryId(countryId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────

    @Transactional
    public AgencyResponse createAgency(CreateAgencyRequest request,
                                       Long adminId) {
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Country", request.getCountryId()));

        Agency agency = Agency.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .country(country)
                .dailyLimit(request.getDailyLimit())
                .active(true)
                .build();

        Agency saved = agencyRepository.save(agency);

        // Auto-create a cash register for the agency
        CashRegister cashRegister = CashRegister.builder()
                .agency(saved)
                .build();
        cashRegisterRepository.save(cashRegister);

        auditService.log(adminId, "AGENCY_CREATED",
                "Agency", saved.getId(),
                "{\"name\":\"" + request.getName() + "\"}");

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────

    @Transactional
    public AgencyResponse updateAgency(Long id, UpdateAgencyRequest request,
                                       Long adminId) {
        Agency agency = findAgency(id);
        agency.setName(request.getName());
        agency.setAddress(request.getAddress());
        agency.setCity(request.getCity());
        agency.setDailyLimit(request.getDailyLimit());
        agencyRepository.save(agency);

        auditService.log(adminId, "AGENCY_UPDATED", "Agency", id, null);
        return toResponse(agency);
    }

    // ─────────────────────────────────────────────────────
    //  ASSIGN MANAGER
    // ─────────────────────────────────────────────────────

    @Transactional
    public AgencyResponse assignManager(Long agencyId, Long managerId,
                                        Long adminId) {
        Agency agency = findAgency(agencyId);

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", managerId));

        if (manager.getRole() != Role.ROLE_MANAGER) {
            throw new IllegalArgumentException(
                    "User must have ROLE_MANAGER to be assigned as agency manager");
        }

        // Update agency pointer
        agency.setManager(manager);
        agencyRepository.save(agency);

        // Update manager's agency
        manager.setAgency(agency);
        userRepository.save(manager);

        auditService.log(adminId, "AGENCY_MANAGER_ASSIGNED",
                "Agency", agencyId,
                "{\"managerId\":" + managerId + "}");

        return toResponse(agency);
    }

    // ─────────────────────────────────────────────────────
    //  SUSPEND / ACTIVATE
    // ─────────────────────────────────────────────────────

    @Transactional
    public void suspendAgency(Long id, Long adminId) {
        Agency agency = findAgency(id);
        agency.setActive(false);
        agencyRepository.save(agency);
        auditService.log(adminId, "AGENCY_SUSPENDED", "Agency", id, null);
    }

    @Transactional
    public void activateAgency(Long id, Long adminId) {
        Agency agency = findAgency(id);
        agency.setActive(true);
        agencyRepository.save(agency);
        auditService.log(adminId, "AGENCY_ACTIVATED", "Agency", id, null);
    }

    // ─────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────

    private Agency findAgency(Long id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", id));
    }

    private AgencyResponse toResponse(Agency a) {
        return AgencyResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .address(a.getAddress())
                .city(a.getCity())
                .countryName(a.getCountry().getName())
                .countryCode(a.getCountry().getCode())
                .managerId(a.getManager() != null
                        ? a.getManager().getId() : null)
                .managerName(a.getManager() != null
                        ? a.getManager().getFirstName() + " "
                        + a.getManager().getLastName() : null)
                .dailyLimit(a.getDailyLimit())
                .active(a.isActive())
                .createdAt(a.getCreatedAt())
                .build();
    }
}