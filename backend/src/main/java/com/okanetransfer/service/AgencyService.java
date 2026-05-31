package com.okanetransfer.service;

import com.okanetransfer.dto.request.AgencyRequest;
import com.okanetransfer.dto.request.AssignManagerRequest;
import com.okanetransfer.dto.response.AgencyResponse;

import java.util.List;

public interface AgencyService {
    List<AgencyResponse> getAllAgencies();
    AgencyResponse getAgencyById(Long id);
    AgencyResponse createAgency(AgencyRequest request);
    AgencyResponse updateAgency(Long id, AgencyRequest request);
    void assignManager(Long agencyId, AssignManagerRequest request);
    void suspendAgency(Long id);
    void activateAgency(Long id);
    List<AgencyResponse> getAgenciesByCountry(Long countryId);
}