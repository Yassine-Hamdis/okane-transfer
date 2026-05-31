package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.AgencyRequest;
import com.okanetransfer.dto.request.AssignManagerRequest;
import com.okanetransfer.dto.response.AgencyResponse;
import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.Country;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.exception.ConflictException;
import com.okanetransfer.exception.NotFoundException;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.CountryRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AgencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgencyServiceImpl implements AgencyService {

    private final AgencyRepository agencyRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;

    public AgencyServiceImpl(AgencyRepository agencyRepository,
                             CountryRepository countryRepository,
                             UserRepository userRepository) {
        this.agencyRepository = agencyRepository;
        this.countryRepository = countryRepository;
        this.userRepository = userRepository;
    }

    private static AgencyResponse toResponse(Agency a) {
        AgencyResponse r = new AgencyResponse();
        r.setId(a.getId());
        r.setName(a.getName());
        r.setAddress(a.getAddress());
        r.setCity(a.getCity());
        r.setDailyLimit(a.getDailyLimit());
        r.setActive(a.isActive());
        r.setCountryId(a.getCountry() != null ? a.getCountry().getId() : null);
        r.setManagerId(a.getManager() != null ? a.getManager().getId() : null);
        return r;
    }

    @Override
    public List<AgencyResponse> getAllAgencies() {
        return agencyRepository.findAll().stream().map(AgencyServiceImpl::toResponse).toList();
    }

    @Override
    public AgencyResponse getAgencyById(Long id) {
        return toResponse(agencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agency not found: " + id)));
    }

    @Override
    @Transactional
    public AgencyResponse createAgency(AgencyRequest request) {
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NotFoundException("Country not found: " + request.getCountryId()));

        Agency a = new Agency();
        a.setName(request.getName());
        a.setAddress(request.getAddress());
        a.setCity(request.getCity());
        a.setDailyLimit(request.getDailyLimit());
        a.setActive(true);
        a.setCountry(country);

        agencyRepository.save(a);
        return toResponse(a);
    }

    @Override
    @Transactional
    public AgencyResponse updateAgency(Long id, AgencyRequest request) {
        Agency a = agencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agency not found: " + id));

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NotFoundException("Country not found: " + request.getCountryId()));

        a.setName(request.getName());
        a.setAddress(request.getAddress());
        a.setCity(request.getCity());
        a.setDailyLimit(request.getDailyLimit());
        a.setCountry(country);

        agencyRepository.save(a);
        return toResponse(a);
    }

    @Override
    @Transactional
    public void assignManager(Long agencyId, AssignManagerRequest request) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new NotFoundException("Agency not found: " + agencyId));

        if (agency.getManager() != null) {
            throw new ConflictException("Agency already has a manager");
        }

        User manager = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.getUserId()));

        if (manager.getRole() != Role.ROLE_MANAGER) {
            throw new ConflictException("User is not ROLE_MANAGER");
        }

        if (agencyRepository.findByManagerId(manager.getId()).isPresent()) {
            throw new ConflictException("Manager already assigned to another agency");
        }

        agency.setManager(manager);
        manager.setAgency(agency); // cohérence

        agencyRepository.save(agency);
        userRepository.save(manager);
    }

    @Override
    @Transactional
    public void suspendAgency(Long id) {
        Agency a = agencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agency not found: " + id));
        a.setActive(false);
        agencyRepository.save(a);
    }

    @Override
    @Transactional
    public void activateAgency(Long id) {
        Agency a = agencyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agency not found: " + id));
        a.setActive(true);
        agencyRepository.save(a);
    }

    @Override
    public List<AgencyResponse> getAgenciesByCountry(Long countryId) {
        return agencyRepository.findByCountryId(countryId).stream().map(AgencyServiceImpl::toResponse).toList();
    }
}