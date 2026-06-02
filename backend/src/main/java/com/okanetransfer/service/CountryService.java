package com.okanetransfer.service;

import com.okanetransfer.dto.response.CountryLookupDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.dto.request.CreateCountryRequest;
import com.okanetransfer.dto.request.UpdateCountryRequest;
import com.okanetransfer.dto.response.CountryResponseDto;
import com.okanetransfer.entity.Country;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.mapper.CountryMapper;
import com.okanetransfer.repository.CountryRepository;
import com.okanetransfer.repository.CurrencyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final CorridorService corridorService;;

    public CountryService(CountryRepository countryRepository, CurrencyRepository currencyRepository, CorridorService corridorService) {
        this.countryRepository = countryRepository;
        this.currencyRepository = currencyRepository;
        this.corridorService = corridorService;
    }

    private void refreshCorridors(Long countryId) {
        corridorService.refreshCorridorsByCountry(countryId);
    }

    @Transactional
    public CountryResponseDto create(CreateCountryRequest request) {

        if (countryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Country name already exists");
        }

        if (countryRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Country code already exists");
        }

        Currency currency = currencyRepository.findById(
                request.getDefaultCurrencyId()
        ).orElseThrow(() ->
                new RuntimeException("Currency not found"));

        Country country = Country.builder()
                .name(request.getName())
                .code(request.getCode())
                .defaultCurrency(currency)
                .build();

        return CountryMapper.toDto(
                countryRepository.save(country)
        );
    }

    @Transactional
    public CountryResponseDto update(
            Long id,
            UpdateCountryRequest request) {

        Country country = countryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Country not found"));

        if (countryRepository.existsByNameAndIdNot(
                request.getName(),
                id)) {
            throw new RuntimeException("Country name already exists");
        }

        if (countryRepository.existsByCodeAndIdNot(
                request.getCode(),
                id)) {
            throw new RuntimeException("Country code already exists");
        }

        Currency currency = currencyRepository.findById(
                        request.getDefaultCurrencyId())
                .orElseThrow(() ->
                        new RuntimeException("Currency not found"));

        country.setName(request.getName());
        country.setCode(request.getCode());
        country.setDefaultCurrency(currency);

        return CountryMapper.toDto(
                countryRepository.save(country)
        );
    }

    @Transactional
    public CountryResponseDto toggleActive(Long id) {

        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found"));

        boolean newState = !country.isActive();
        country.setActive(newState);

        Country saved = countryRepository.save(country);

        //  cascade only if disable OR enable
        refreshCorridors(id);

        return CountryMapper.toDto(saved);
    }

    @Transactional
    public CountryResponseDto toggleSending(Long id) {

        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found"));

        country.setAllowsSending(!country.isAllowsSending());

        Country saved = countryRepository.save(country);

        //  recalcul corridors ALWAYS (important rule)
        refreshCorridors(id);

        return CountryMapper.toDto(saved);
    }

    @Transactional
    public CountryResponseDto toggleReceiving(Long id) {

        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found"));

        country.setAllowsReceiving(!country.isAllowsReceiving());

        Country saved = countryRepository.save(country);

        //  recalcul corridors ALWAYS
        refreshCorridors(id);

        return CountryMapper.toDto(saved);
    }


    @Transactional(readOnly = true)
    public PaginationResponse<CountryResponseDto> search(
            Boolean active,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {

        //  sécuriser tri
        List<String> allowedSorts = List.of("id", "name", "code");

        if (!allowedSorts.contains(sortBy)) {
            sortBy = "id";
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Country> result = countryRepository.searchCountries(
                active,
                keyword,
                pageable
        );

        List<CountryResponseDto> content = result.getContent()
                .stream()
                .map(CountryMapper::toDto)
                .toList();

        return PaginationResponse.<CountryResponseDto>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }
    @Transactional(readOnly = true)
    public List<CountryLookupDto> searchLookup(String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        return countryRepository.searchByKeyword(keyword)
                .stream()
                .map(c -> new CountryLookupDto(
                        c.getId(),
                        c.getName(),
                        c.getCode()
                ))
                .toList();
    }

}
