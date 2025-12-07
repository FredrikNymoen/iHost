package no.ntnu.prog2007.ihostapi.model.dto

/**
 * Standardized error response structure for all API endpoints.
 *
 * Provides a consistent error format that clients can reliably parse,
 * regardless of which endpoint or exception type caused the error.
 *
 * Used by [GlobalExceptionHandler] to transform exceptions into HTTP responses.
 *
 * @property error Machine-readable error code (e.g., "VALIDATION_ERROR", "NOT_FOUND")
 * @property message Human-readable description of what went wrong
 *
 * @see no.ntnu.prog2007.ihostapi.exception.GlobalExceptionHandler
 */
data class ErrorResponse(
    val error: String,
    val message: String
)
