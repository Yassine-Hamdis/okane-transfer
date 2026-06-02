package com.okanetransfer.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.okanetransfer.dto.request.CreateFeeGridRequest;
import com.okanetransfer.dto.request.FeeGridProposalRequest;
import com.okanetransfer.dto.response.*;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.ExchangeRateRepository;
import com.okanetransfer.repository.FeeGridRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeGridRepository feeGridRepository;
    private final CorridorRepository corridorRepository;
    private final ExchangeRateRepository exchangeRateRepository;

//    @Transactional(readOnly = true)
//    public FeeGridProposalResponse propose(FeeGridProposalRequest request) {
//
//        Corridor corridor = corridorRepository.findById(request.getCorridorId())
//                .orElseThrow(() -> new RuntimeException("Corridor not found"));
//
//        List<String> warnings = new ArrayList<>();
//
//        // 1. simulate FX
//        BigDecimal rate = exchangeRateRepository
//                .findByCorridorIdAndIsCurrentTrue(corridor.getId())
//                .orElseThrow()
//                .getRate();
//
//        BigDecimal amountAgency =
//                request.getTestAmount().multiply(rate);
//
//        // 2. simulate grid logic (WITHOUT saving)
//        FeeGrid temp = FeeGrid.builder()
//                .corridor(corridor)
//                .transferType(request.getTransferType())
//                .minAmount(request.getMinAmount())
//                .maxAmount(request.getMaxAmount())
//                .feeFixedAmount(request.getFeeFixedAmount())
//                .feePercentage(request.getFeePercentage())
//                .agencySharePercent(request.getAgencySharePercent())
//                .centralSharePercent(request.getCentralSharePercent())
//                .build();
//
//        validateWithoutSave(temp);
//
//        // 3. compute fees
//        BigDecimal feeFixed = Optional.ofNullable(temp.getFeeFixedAmount())
//                .orElse(BigDecimal.ZERO);
//
//        BigDecimal feePercent =
//                amountAgency.multiply(temp.getFeePercentage())
//                        .divide(BigDecimal.valueOf(100));
//
//        BigDecimal totalFee = feeFixed.add(feePercent);
//
//        BigDecimal agencyShare =
//                totalFee.multiply(BigDecimal.valueOf(temp.getAgencySharePercent()))
//                        .divide(BigDecimal.valueOf(100));
//
//        BigDecimal centralShare =
//                totalFee.multiply(BigDecimal.valueOf(temp.getCentralSharePercent()))
//                        .divide(BigDecimal.valueOf(100));
//
//        FeeSimulationResponse simulation =
//                FeeSimulationResponse.builder()
//                        .clientAmount(request.getTestAmount())
//                        .exchangeRate(rate)
//                        .amountInAgencyCurrency(amountAgency)
//                        .feeFixed(feeFixed)
//                        .feePercentageValue(feePercent)
//                        .totalFee(totalFee)
//                        .agencyShare(agencyShare)
//                        .centralShare(centralShare)
//                        .build();
//
//        return FeeGridProposalResponse.builder()
//                .simulation(simulation)
//                .valid(warnings.isEmpty())
//                .warnings(warnings)
//                .build();
//    }
//
//    @Transactional
//    public FeeGrid createFeeGrid(CreateFeeGridRequest request) {
//
//        Corridor corridor = corridorRepository.findById(request.getCorridorId())
//                .orElseThrow(() -> new RuntimeException("Corridor not found"));
//
//        FeeGrid newGrid = FeeGrid.builder()
//                .corridor(corridor)
//                .transferType(request.getTransferType())
//                .minAmount(request.getMinAmount())
//                .maxAmount(request.getMaxAmount())
//                .feeFixedAmount(request.getFeeFixedAmount())
//                .feePercentage(request.getFeePercentage())
//                .agencySharePercent(request.getAgencySharePercent())
//                .centralSharePercent(request.getCentralSharePercent())
//                .active(true)
//                .build();
//
//        validateFeeGrid(newGrid);
//
//        return feeGridRepository.save(newGrid);
//    }
//
//    // =========================
//    // VALIDATION CORE
//    // =========================
//    private void validateFeeGrid(FeeGrid newGrid) {
//
//        // A. min < max
//        if (newGrid.getMinAmount() == null ||
//                newGrid.getMaxAmount() == null ||
//                newGrid.getMinAmount().compareTo(newGrid.getMaxAmount()) >= 0) {
//
//            throw new RuntimeException("Invalid range: minAmount must be < maxAmount");
//        }
//
//        // B. share must = 100
//        int totalShare =
//                newGrid.getAgencySharePercent() +
//                        newGrid.getCentralSharePercent();
//
//        if (totalShare != 100) {
//            throw new RuntimeException("Agency + Central share must equal 100%");
//        }
//
//        // C. load existing ranges
//        List<FeeGrid> existing = feeGridRepository
//                .findByCorridorIdAndTransferTypeAndActiveTrueOrderByMinAmountAsc(
//                        newGrid.getCorridor().getId(),
//                        newGrid.getTransferType()
//                );
//
//        // D. overlap check
//        for (FeeGrid g : existing) {
//
//            boolean overlap =
//                    newGrid.getMinAmount().compareTo(g.getMaxAmount()) < 0 &&
//                            newGrid.getMaxAmount().compareTo(g.getMinAmount()) > 0;
//
//            if (overlap) {
//                throw new RuntimeException(
//                        "FeeGrid range overlaps existing range: [" +
//                                g.getMinAmount() + " - " + g.getMaxAmount() + "]"
//                );
//            }
//        }
//
//        // E. continuity check (IMPORTANT RULE)
//        Optional<BigDecimal> lastMax = existing.stream()
//                .map(FeeGrid::getMaxAmount)
//                .max(BigDecimal::compareTo);
//
//        if (lastMax.isPresent()) {
//
//            if (newGrid.getMinAmount().compareTo(lastMax.get()) != 0) {
//                throw new RuntimeException(
//                        "New range must start at: " + lastMax.get()
//                );
//            }
//        }
//
//        // F. first range must start at 0 (optional rule)
//        if (existing.isEmpty() &&
//                newGrid.getMinAmount().compareTo(BigDecimal.ZERO) != 0) {
//            throw new RuntimeException("First range must start at 0");
//        }
//    }


    private void validateFeeGridRules(FeeGrid newGrid, List<FeeGrid> existing) {

        // A. min < max
        if (newGrid.getMinAmount() == null ||
                newGrid.getMaxAmount() == null ||
                newGrid.getMinAmount().compareTo(newGrid.getMaxAmount()) >= 0) {
            throw new RuntimeException("Invalid range: minAmount must be < maxAmount");
        }

        // B. share must = 100
        int totalShare =
                newGrid.getAgencySharePercent() +
                        newGrid.getCentralSharePercent();

        if (totalShare != 100) {
            throw new RuntimeException("Agency + Central share must equal 100%");
        }

        // C. overlap
        for (FeeGrid g : existing) {

            boolean overlap =
                    newGrid.getMinAmount().compareTo(g.getMaxAmount()) < 0 &&
                            newGrid.getMaxAmount().compareTo(g.getMinAmount()) > 0;

            if (overlap) {
                throw new RuntimeException(
                        "Overlap detected: [" + g.getMinAmount() + " - " + g.getMaxAmount() + "]"
                );
            }
        }

        // D. continuity
        Optional<BigDecimal> lastMax = existing.stream()
                .map(FeeGrid::getMaxAmount)
                .max(BigDecimal::compareTo);

        if (lastMax.isPresent()
                && newGrid.getMinAmount().compareTo(lastMax.get()) != 0) {
            throw new RuntimeException("Gap detected: must start at " + lastMax.get());
        }

        // E. first must start at 0
        if (existing.isEmpty()
                && newGrid.getMinAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("First grid must start at 0");
        }
    }

    private void validateFeeGrid(FeeGrid newGrid) {

        List<FeeGrid> existing =
                feeGridRepository.findByCorridorIdAndTransferTypeAndActiveTrueOrderByMinAmountAsc(
                        newGrid.getCorridor().getId(),
                        newGrid.getTransferType()
                );

        validateFeeGridRules(newGrid, existing);
    }

    @Transactional(readOnly = true)
    public FeeGridProposalResponse propose(FeeGridProposalRequest request) {

        Corridor corridor = corridorRepository.findById(request.getCorridorId())
                .orElseThrow(() -> new RuntimeException("Corridor not found"));

        List<String> warnings = new ArrayList<>();

        // 1. FX rate
        BigDecimal rate = exchangeRateRepository
                .findByCorridorIdAndIsCurrentTrue(corridor.getId())
                .orElseThrow()
                .getRate();

        BigDecimal amountAgency =
                request.getTestAmount().multiply(rate);

        // 2. existing grids (IMPORTANT)
        List<FeeGrid> existing =
                feeGridRepository.findByCorridorIdAndTransferTypeAndActiveTrueOrderByMinAmountAsc(
                        corridor.getId(),
                        request.getTransferType()
                );

        // 3. build temp grid
        FeeGrid temp = FeeGrid.builder()
                .corridor(corridor)
                .transferType(request.getTransferType())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .feeFixedAmount(request.getFeeFixedAmount())
                .feePercentage(request.getFeePercentage())
                .agencySharePercent(request.getAgencySharePercent())
                .centralSharePercent(request.getCentralSharePercent())
                .build();

        // 4. VALIDATION (NO SAVE)
        validateFeeGridRules(temp, existing);

        // 5. FEES
        BigDecimal feeFixed = Optional.ofNullable(temp.getFeeFixedAmount())
                .orElse(BigDecimal.ZERO);

        BigDecimal feePercent =
                amountAgency.multiply(temp.getFeePercentage())
                        .divide(BigDecimal.valueOf(100));

        BigDecimal totalFee = feeFixed.add(feePercent);

        BigDecimal agencyShare =
                totalFee.multiply(BigDecimal.valueOf(temp.getAgencySharePercent()))
                        .divide(BigDecimal.valueOf(100));

        BigDecimal centralShare =
                totalFee.multiply(BigDecimal.valueOf(temp.getCentralSharePercent()))
                        .divide(BigDecimal.valueOf(100));

        // 6. RESPONSE
        FeeSimulationResponse simulation =
                FeeSimulationResponse.builder()
                        .clientAmount(request.getTestAmount())
                        .exchangeRate(rate)
                        .amountInAgencyCurrency(amountAgency)
                        .feeFixed(feeFixed)
                        .feePercentageValue(feePercent)
                        .totalFee(totalFee)
                        .agencyShare(agencyShare)
                        .centralShare(centralShare)
                        .build();

        return FeeGridProposalResponse.builder()
                .simulation(simulation)
                .valid(true)
                .warnings(warnings)
                .build();
    }

    @Transactional
    public FeeGrid createFeeGrid(CreateFeeGridRequest request) {

        Corridor corridor = corridorRepository.findById(request.getCorridorId())
                .orElseThrow(() -> new RuntimeException("Corridor not found"));

        FeeGrid newGrid = FeeGrid.builder()
                .corridor(corridor)
                .transferType(request.getTransferType())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .feeFixedAmount(request.getFeeFixedAmount())
                .feePercentage(request.getFeePercentage())
                .agencySharePercent(request.getAgencySharePercent())
                .centralSharePercent(request.getCentralSharePercent())
                .active(true)
                .build();

        validateFeeGrid(newGrid);

        return feeGridRepository.save(newGrid);
    }

    @Transactional(readOnly = true)
    public FeeGridPreviewResponse getFeePreview(
            Long corridorId,
            TransferType transferType
    ) {

        List<FeeGrid> grids = feeGridRepository
                .findByCorridorIdAndTransferTypeAndActiveTrueOrderByMinAmountAsc(
                        corridorId,
                        transferType
                );

        if (grids.isEmpty()) {
            throw new RuntimeException("No active fee grids found");
        }

        List<FeeGridPreviewItemDto> tiers = grids.stream()
                .map(g -> FeeGridPreviewItemDto.builder()
                        .id(g.getId())
                        .minAmount(g.getMinAmount())
                        .maxAmount(g.getMaxAmount())
                        .feeFixedAmount(g.getFeeFixedAmount())
                        .feePercentage(g.getFeePercentage())
                        .agencySharePercent(g.getAgencySharePercent())
                        .centralSharePercent(g.getCentralSharePercent())
                        .build()
                )
                .toList();

        FeeGrid first = grids.get(0);

        return FeeGridPreviewResponse.builder()
                .corridorId(corridorId)
                .transferType(transferType.name())
                .tiers(tiers)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FeeGrid> getExportData(Long corridorId, TransferType type) {

        return feeGridRepository
                .findByCorridorIdAndTransferTypeAndActiveTrueOrderByMinAmountAsc(
                        corridorId,
                        type
                );
    }

    public byte[] exportCsv(Long corridorId, TransferType type) {
        List<FeeGrid> grids = getExportData(corridorId, type);
        StringWriter sw = new StringWriter();

        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("Min Amount", "Max Amount", "Fixed Fee", "Percentage", "Agency %", "Central %")
                    .build();

            try (CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
                for (FeeGrid g : grids) {
                    printer.printRecord(
                            g.getMinAmount(),
                            g.getMaxAmount(),
                            g.getFeeFixedAmount(),
                            g.getFeePercentage(),
                            g.getAgencySharePercent(),
                            g.getCentralSharePercent()
                    );
                }
            }

            return sw.toString().getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la génération du fichier CSV.", e
            );
        }
    }

    public byte[] exportPdf(Long corridorId, TransferType type) {
        List<FeeGrid> grids = getExportData(corridorId, type);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);

            try (Document document = new Document(pdfDoc)) {

                document.add(new Paragraph("Fee Grid Export"));
                document.add(new Paragraph("Corridor ID: " + corridorId));
                document.add(new Paragraph("Transfer Type: " + type.name()));
                document.add(new Paragraph("\n"));

                // Création d'une table à 6 colonnes prenant toute la largeur
                Table table = new Table(6);
                table.useAllAvailableWidth();

                // En-têtes du tableau
                Stream.of("Min", "Max", "Fixed", "%", "Agency", "Central")
                        .forEach(table::addHeaderCell);

                // Données du tableau
                for (FeeGrid g : grids) {
                    table.addCell(g.getMinAmount() != null ? g.getMinAmount().toString() : "");
                    table.addCell(g.getMaxAmount() != null ? g.getMaxAmount().toString() : "");
                    table.addCell(String.valueOf(g.getFeeFixedAmount()));
                    table.addCell(String.valueOf(g.getFeePercentage()));
                    table.addCell(String.valueOf(g.getAgencySharePercent()));
                    table.addCell(String.valueOf(g.getCentralSharePercent()));
                }

                document.add(table);
            }

            return out.toByteArray();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la génération du fichier PDF.", e
            );
        }
    }
    @Transactional(readOnly = true)
    public AgentFeeGridDto getApplicableFeeGrid(
            Long corridorId,
            TransferType transferType,
            BigDecimal amount) {

        FeeGrid feeGrid = feeGridRepository
                .findApplicableFeeGrid(
                        corridorId,
                        transferType,
                        amount
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No active fee grid found for amount: " + amount));

        Corridor corridor = feeGrid.getCorridor();

        return AgentFeeGridDto.builder()
                .feeGridId(feeGrid.getId())
                .corridorId(corridor.getId())

                .sourceCountry(
                        corridor.getSourceCountry().getName()
                )
                .destinationCountry(
                        corridor.getDestinationCountry().getName()
                )

                .sourceCurrency(
                        corridor.getSourceCurrency().getCode()
                )
                .destinationCurrency(
                        corridor.getDestinationCurrency().getCode()
                )

                .transferType(feeGrid.getTransferType())

                .minAmount(feeGrid.getMinAmount())
                .maxAmount(feeGrid.getMaxAmount())

                .feeFixedAmount(feeGrid.getFeeFixedAmount())
                .feePercentage(feeGrid.getFeePercentage())

                .agencySharePercent(
                        feeGrid.getAgencySharePercent()
                )
                .centralSharePercent(
                        feeGrid.getCentralSharePercent()
                )
                .build();
    }
}