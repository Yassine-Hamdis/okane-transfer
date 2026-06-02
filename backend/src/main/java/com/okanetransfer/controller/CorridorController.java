package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateCorridorRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.CorridorResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.service.CorridorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CorridorController {

    private final CorridorService corridorService;


    @PostMapping("/admin/corridors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CorridorResponseDto>> createCorridor(
            @RequestBody CreateCorridorRequest request) {

        CorridorResponseDto corridor = corridorService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CorridorResponseDto>builder()
                        .success(true)
                        .message("Corridor created successfully")
                        .data(corridor)
                        .build());
    }


    @PatchMapping("/admin/corridors/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CorridorResponseDto>> toggleActive(
            @PathVariable Long id) {

        CorridorResponseDto corridor = corridorService.toggleActive(id);

        return ResponseEntity.ok(
                ApiResponse.<CorridorResponseDto>builder()
                        .success(true)
                        .message("Corridor status updated successfully")
                        .data(corridor)
                        .build()
        );
    }


    @GetMapping("/admin/corridors/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<CorridorResponseDto>>> search(

            @RequestParam(required = false) Long sourceCountryId,
            @RequestParam(required = false) Long destinationCountryId,
            @RequestParam(required = false) Boolean active,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        PaginationResponse<CorridorResponseDto> result =
                corridorService.search(
                        sourceCountryId,
                        destinationCountryId,
                        active,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(
                ApiResponse.<PaginationResponse<CorridorResponseDto>>builder()
                        .success(true)
                        .message("Corridors retrieved successfully")
                        .data(result)
                        .build()
        );
    }
    //search corridor by destination country id pour agent

        @GetMapping("/agent/destination/{countryId}")
        @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<CorridorResponseDto>> getCorridor(
                @RequestParam Long sourceCountryId,
                @RequestParam Long destinationCountryId) {

            CorridorResponseDto corridor =
                    corridorService.getCorridor(sourceCountryId, destinationCountryId);

            return ResponseEntity.ok(
                    ApiResponse.<CorridorResponseDto>builder()
                            .success(true)
                            .message("Corridor found successfully")
                            .data(corridor)
                            .build()
            );
        }

}
