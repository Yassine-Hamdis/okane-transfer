package com.okanetransfer.util;

import com.okanetransfer.entity.*;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.ExchangeRate;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.ExchangeRateRepository;
import com.okanetransfer.repository.FeeGridRepository;

/**
 * Seeds the database with essential base data on startup.
 * Runs ONLY if the database is empty (checks admin count).
 *
 * What gets seeded:
 *  - 1 ROLE_ADMIN user
 *  - Base countries
 *  - Base currencies
 */
@Component
public class DataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired private UserRepository     userRepository;
    @Autowired private CountryRepository  countryRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private AgencyRepository   agencyRepository;
    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private PasswordEncoder    passwordEncoder;
    @Autowired private CorridorRepository     corridorRepository;
    @Autowired private ExchangeRateRepository exchangeRateRepository;
    @Autowired private FeeGridRepository      feeGridRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (userRepository.count() > 0) {
            log.info("[SEEDER] Database already has data — skipping seed.");
            return;
        }
        log.info("[SEEDER] Empty database detected — seeding base data...");
        seedCountries();
        seedCurrencies();
        seedCorridorsRatesAndFees();
        seedAdminUser();
        seedDemoAgencyWithManager();
        log.info("[SEEDER] Done.");
    }

    // ─────────────────────────────────────────────────────
    //  COUNTRIES
    // ─────────────────────────────────────────────────────

    private void seedCountries() {
        List<Country> countries = List.of(
                country("Morocco",       "MA", true,  true),
                country("France",        "FR", true,  true),
                country("Senegal",       "SN", true,  true),
                country("Kenya",         "KE", true,  true),
                country("United States", "US", true,  true),
                country("United Kingdom","GB", true,  true),
                country("Spain",         "ES", true,  true),
                country("Belgium",       "BE", true,  true),
                country("Canada",        "CA", true,  true),
                country("Ivory Coast",   "CI", true,  true)
        );
        countryRepository.saveAll(countries);
        log.info("[SEEDER] {} countries saved.", countries.size());
    }

    private Country country(String name, String code,
                            boolean send, boolean receive) {
        return Country.builder()
                .name(name)
                .code(code)
                .allowsSending(send)
                .allowsReceiving(receive)
                .active(true)
                .build();
    }

    // ─────────────────────────────────────────────────────
    //  CURRENCIES
    // ─────────────────────────────────────────────────────

    private void seedCurrencies() {
        List<Currency> currencies = List.of(
                currency("MAD", "Moroccan Dirham", "د.م"),
                currency("EUR", "Euro",            "€"),
                currency("USD", "US Dollar",       "$"),
                currency("GBP", "British Pound",   "£"),
                currency("XOF", "CFA Franc",       "Fr"),
                currency("KES", "Kenyan Shilling", "KSh"),
                currency("CAD", "Canadian Dollar", "CA$")
        );
        currencyRepository.saveAll(currencies);
        log.info("[SEEDER] {} currencies saved.", currencies.size());
    }

    private Currency currency(String code, String name, String symbol) {
        return Currency.builder()
                .code(code)
                .name(name)
                .symbol(symbol)
                .active(true)
                .build();
    }

    // ─────────────────────────────────────────────────────
    //  ADMIN USER
    // ─────────────────────────────────────────────────────

    private void seedAdminUser() {
        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email("admin@okanetransfer.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .phone("+212600000000")
                .role(Role.ROLE_ADMIN)
                .active(true)
                .twoFactorEnabled(false)
                .mustChangePassword(false)
                .build();

        userRepository.save(admin);
        log.info("[SEEDER] Admin user created: admin@okanetransfer.com / Admin@1234");
    }

    // ─────────────────────────────────────────────────────
    //  DEMO AGENCY + MANAGER + AGENT
    // ─────────────────────────────────────────────────────

    private void seedDemoAgencyWithManager() {
        Country morocco = countryRepository.findByCode("MA")
                .orElseThrow(() -> new RuntimeException("Morocco not found"));

        // Create agency first (manager is null initially)
        Agency agency = Agency.builder()
                .name("Okane Casablanca Central")
                .address("123 Boulevard Mohammed V")
                .city("Casablanca")
                .country(morocco)
                .dailyLimit(new BigDecimal("500000.00"))
                .active(true)
                .build();
        agencyRepository.save(agency);

        // Create manager
        User manager = User.builder()
                .firstName("Youssef")
                .lastName("Alami")
                .email("manager@okanetransfer.com")
                .password(passwordEncoder.encode("Manager@1234"))
                .phone("+212611000001")
                .role(Role.ROLE_MANAGER)
                .active(true)
                .agency(agency)
                .twoFactorEnabled(false)
                .mustChangePassword(false)
                .build();
        userRepository.save(manager);

        // Create agent
        User agent = User.builder()
                .firstName("Fatima")
                .lastName("Benali")
                .email("agent@okanetransfer.com")
                .password(passwordEncoder.encode("Agent@1234"))
                .phone("+212611000002")
                .role(Role.ROLE_AGENT)
                .active(true)
                .agency(agency)
                .twoFactorEnabled(false)
                .mustChangePassword(false)
                .build();
        userRepository.save(agent);

        // Create demo client
        User client = User.builder()
                .firstName("Hassan")
                .lastName("Ouahbi")
                .email("client@okanetransfer.com")
                .password(passwordEncoder.encode("Client@1234"))
                .phone("+212661000003")
                .role(Role.ROLE_CLIENT)
                .active(true)
                .twoFactorEnabled(false)
                .mustChangePassword(false)
                .build();
        userRepository.save(client);

        // Assign manager to agency
        agency.setManager(manager);
        agencyRepository.save(agency);

        // Create cash register for agency
        CashRegister cashRegister = CashRegister.builder()
                .agency(agency)
                .build();
        cashRegisterRepository.save(cashRegister);

        log.info("[SEEDER] Demo agency created: Okane Casablanca Central");
        log.info("[SEEDER] manager@okanetransfer.com / Manager@1234");
        log.info("[SEEDER] agent@okanetransfer.com   / Agent@1234");
        log.info("[SEEDER] client@okanetransfer.com  / Client@1234");
    }

    private void seedCorridorsRatesAndFees() {

        // ── Fetch seeded base data ────────────────────────
        Country morocco = countryRepository.findByCode("MA")
                .orElseThrow(() -> new RuntimeException("MA not found"));
        Country france  = countryRepository.findByCode("FR")
                .orElseThrow(() -> new RuntimeException("FR not found"));
        Country senegal = countryRepository.findByCode("SN")
                .orElseThrow(() -> new RuntimeException("SN not found"));
        Country kenya   = countryRepository.findByCode("KE")
                .orElseThrow(() -> new RuntimeException("KE not found"));

        Currency mad = currencyRepository.findByCode("MAD")
                .orElseThrow(() -> new RuntimeException("MAD not found"));
        Currency eur = currencyRepository.findByCode("EUR")
                .orElseThrow(() -> new RuntimeException("EUR not found"));
        Currency xof = currencyRepository.findByCode("XOF")
                .orElseThrow(() -> new RuntimeException("XOF not found"));
        Currency kes = currencyRepository.findByCode("KES")
                .orElseThrow(() -> new RuntimeException("KES not found"));

        // ── Corridors ─────────────────────────────────────
        Corridor maToSn = createCorridor(morocco, senegal, mad, xof);
        Corridor frToMa = createCorridor(france,  morocco, eur, mad);
        Corridor maToKe = createCorridor(morocco, kenya,   mad, kes);

        corridorRepository.saveAll(List.of(maToSn, frToMa, maToKe));
        log.info("[SEEDER] 3 corridors saved.");

        // ── Exchange Rates ────────────────────────────────
        exchangeRateRepository.saveAll(List.of(
                rate(maToSn, new BigDecimal("60.50")),   // 1 MAD = 60.50 XOF
                rate(frToMa, new BigDecimal("10.80")),   // 1 EUR = 10.80 MAD
                rate(maToKe, new BigDecimal("13.20"))    // 1 MAD = 13.20 KES
        ));
        log.info("[SEEDER] 3 exchange rates saved.");

        // ── Fee Grids — MA → SN (MAD, STANDARD) ──────────
        feeGridRepository.saveAll(List.of(

                // MA → SN  |  0 - 1000 MAD  |  fixed 15 MAD
                FeeGrid.builder()
                        .corridor(maToSn).currency(mad)
                        .minAmount(new BigDecimal("0"))
                        .maxAmount(new BigDecimal("1000"))
                        .feeFixedAmount(new BigDecimal("15.00"))
                        .feePercentage(BigDecimal.ZERO)
                        .agencySharePercent(30).centralSharePercent(70)
                        .transferType(TransferType.STANDARD).active(true)
                        .build(),

                // MA → SN  |  1001 - 5000 MAD  |  1.5%
                FeeGrid.builder()
                        .corridor(maToSn).currency(mad)
                        .minAmount(new BigDecimal("1001"))
                        .maxAmount(new BigDecimal("5000"))
                        .feeFixedAmount(BigDecimal.ZERO)
                        .feePercentage(new BigDecimal("1.50"))
                        .agencySharePercent(25).centralSharePercent(75)
                        .transferType(TransferType.STANDARD).active(true)
                        .build(),

                // MA → SN  |  5001 - 20000 MAD  |  1%
                FeeGrid.builder()
                        .corridor(maToSn).currency(mad)
                        .minAmount(new BigDecimal("5001"))
                        .maxAmount(new BigDecimal("20000"))
                        .feeFixedAmount(BigDecimal.ZERO)
                        .feePercentage(new BigDecimal("1.00"))
                        .agencySharePercent(20).centralSharePercent(80)
                        .transferType(TransferType.STANDARD).active(true)
                        .build(),

                // MA → SN  EXPRESS  |  0 - 5000 MAD  |  fixed 25 + 0.5%
                FeeGrid.builder()
                        .corridor(maToSn).currency(mad)
                        .minAmount(new BigDecimal("0"))
                        .maxAmount(new BigDecimal("5000"))
                        .feeFixedAmount(new BigDecimal("25.00"))
                        .feePercentage(new BigDecimal("0.50"))
                        .agencySharePercent(30).centralSharePercent(70)
                        .transferType(TransferType.EXPRESS).active(true)
                        .build(),

                // FR → MA  |  0 - 500 EUR  |  fixed 5 EUR
                FeeGrid.builder()
                        .corridor(frToMa).currency(eur)
                        .minAmount(new BigDecimal("0"))
                        .maxAmount(new BigDecimal("500"))
                        .feeFixedAmount(new BigDecimal("5.00"))
                        .feePercentage(BigDecimal.ZERO)
                        .agencySharePercent(30).centralSharePercent(70)
                        .transferType(TransferType.STANDARD).active(true)
                        .build(),

                // FR → MA  |  501 - 5000 EUR  |  1.2%
                FeeGrid.builder()
                        .corridor(frToMa).currency(eur)
                        .minAmount(new BigDecimal("501"))
                        .maxAmount(new BigDecimal("5000"))
                        .feeFixedAmount(BigDecimal.ZERO)
                        .feePercentage(new BigDecimal("1.20"))
                        .agencySharePercent(25).centralSharePercent(75)
                        .transferType(TransferType.STANDARD).active(true)
                        .build()
        ));

        log.info("[SEEDER] 6 fee grids saved.");
    }

    private Corridor createCorridor(Country source, Country dest,
                                    Currency srcCurr, Currency destCurr) {
        return Corridor.builder()
                .sourceCountry(source)
                .destinationCountry(dest)
                .sourceCurrency(srcCurr)
                .destinationCurrency(destCurr)
                .active(true)
                .build();
    }

    private ExchangeRate rate(Corridor corridor, BigDecimal rateValue) {
        return ExchangeRate.builder()
                .corridor(corridor)
                .rate(rateValue)
                .source("MANUAL")
                .updatedBy(null)
                .isCurrent(true)
                .build();
    }

}