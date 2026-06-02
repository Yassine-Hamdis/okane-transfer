package com.okanetransfer.service;

import com.okanetransfer.dto.response.CurrencyResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Country;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.mapper.CurrencyMapper;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CountryRepository;
import com.okanetransfer.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class  CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CountryRepository countryRepository;
    private final CorridorRepository corridorRepository;
    ;
    @Transactional
    public Currency createCurrency(Currency currency) {

        //  check code exists
        if (currencyRepository.existsByCode(currency.getCode())) {
            throw new RuntimeException("Currency code already exists: " + currency.getCode());
        }

        //  check symbol exists
        if (currencyRepository.existsBySymbol(currency.getSymbol())) {
            throw new RuntimeException("Currency symbol already exists: " + currency.getSymbol());
        }
        //  check name exists
        if (currencyRepository.existsByName(currency.getName())) {
            throw new RuntimeException("Currency name already exists: " + currency.getName());
        }

        return currencyRepository.save(currency);
    }
    @Transactional
    public Currency updateCurrency(Long id, Currency updatedCurrency) {

        //  1. vérifier existence
        Currency existing = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found with id: " + id));

        //  2. check code unique (sauf lui-même)
        if (currencyRepository.existsByCodeAndIdNot(updatedCurrency.getCode(), id)) {
            throw new RuntimeException("Currency code already exists");
        }

        //  3. check symbol unique (sauf lui-même)
        if (currencyRepository.existsBySymbolAndIdNot(updatedCurrency.getSymbol(), id)) {
            throw new RuntimeException("Currency symbol already exists");
        }

        // ✏️ 4. update fields
        existing.setCode(updatedCurrency.getCode());
        existing.setName(updatedCurrency.getName());
        existing.setSymbol(updatedCurrency.getSymbol());
        existing.setActive(updatedCurrency.isActive());

        return currencyRepository.save(existing);
    }

    @Transactional
    public CurrencyResponseDto toggleActive(Long id) {

        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found"));

        boolean newState = !currency.isActive();
        currency.setActive(newState);

        Currency saved = currencyRepository.save(currency);

        //  CASCADE BULK ONLY ON DISABLE
        if (!newState) {
            countryRepository.disableByCurrency(id);
            corridorRepository.disableByCurrency(id);
        }

        return CurrencyMapper.toDto(saved);
    }


    @Transactional(readOnly = true)
    public PaginationResponse<CurrencyResponseDto> search(
            Boolean active,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (keyword == null) {
            keyword = "";
        }

        List<String> allowedSorts = List.of("id", "code", "name", "symbol", "active");

        if (sortBy == null || !allowedSorts.contains(sortBy)) {
            sortBy = "id";
        }

        if (direction == null ||
                (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            direction = "asc";
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Currency> result =
                currencyRepository.searchCurrencies(active, keyword, pageable);

        List<CurrencyResponseDto> currencies =
                result.getContent()
                        .stream()
                        .map(CurrencyMapper::toDto)
                        .toList();

        return PaginationResponse.<CurrencyResponseDto>builder()
                .content(currencies)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }
}