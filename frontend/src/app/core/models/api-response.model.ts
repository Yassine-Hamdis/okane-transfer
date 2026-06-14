/**
 * Generic wrapper for ALL backend responses.
 * Backend uses @JsonInclude(NON_NULL) so `data` may be absent
 * on void operations (suspend, activate, cancel, toggle, etc.)
 *
 * Services MUST unwrap with:
 *   map(res => res.data!)   → for operations that return an object
 *   map(res => res.message) → for void operations
 *
 * Components MUST NEVER interact with ApiResponse directly.
 */
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

/**
 * Shape of the error body returned by the GlobalExceptionHandler.
 * Available inside the HttpErrorResponse.error field.
 *
 * HTTP status codes:
 *   400 → validation failed  (data contains fieldErrors map)
 *   401 → not authenticated
 *   403 → forbidden
 *   404 → not found
 *   500 → unexpected server error
 */
export interface ApiErrorResponse {
  success: false;
  message: string;
  data?: Record<string, string>; // field → error message (400 only)
}
