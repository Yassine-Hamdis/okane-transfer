package com.okanetransfer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // don't serialize null fields
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;

    // ─────────────────────────────────────────────────────
    //  STATIC FACTORIES
    // ─────────────────────────────────────────────────────

    /**
     * Success with data.
     * Use for: GET, POST, PUT, PATCH that return something.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Success without data.
     * Use for: suspend, activate, delete, mark-as-read, etc.
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Error with message.
     * Used by GlobalExceptionHandler.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * Error with message and data (e.g. validation field errors).
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}