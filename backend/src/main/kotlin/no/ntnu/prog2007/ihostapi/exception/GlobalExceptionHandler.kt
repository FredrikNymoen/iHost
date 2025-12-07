package no.ntnu.prog2007.ihostapi.exception

import no.ntnu.prog2007.ihostapi.model.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.logging.Logger

/**
 * Global exception handler that converts exceptions into standardized HTTP error responses.
 *
 * This `@RestControllerAdvice` intercepts exceptions thrown by any controller or service
 * and maps them to appropriate HTTP status codes with consistent JSON error bodies.
 *
 * Benefits of centralized exception handling:
 * - Consistent error response format across all endpoints
 * - Prevents sensitive stack traces from leaking to clients
 * - Reduces boilerplate try-catch blocks in controllers
 * - Centralizes logging for debugging and monitoring
 *
 * Error response format: `{"error": "ERROR_CODE", "message": "Human-readable message"}`
 *
 * @see ErrorResponse for the error response DTO structure
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = Logger.getLogger(GlobalExceptionHandler::class.java.name)

    /**
     * Handles authentication failures and missing/invalid tokens.
     *
     * Returns HTTP 401 with an UNAUTHORIZED error code. Clients should
     * re-authenticate with Firebase Auth when receiving this response.
     *
     * @param ex The caught UnauthorizedException
     * @return HTTP 401 with error details
     */
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ErrorResponse> {
        logger.warning("Unauthorized: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("UNAUTHORIZED", ex.message ?: "Unauthorized"))
    }

    /**
     * Handles authorization failures when user lacks permission.
     *
     * Returns HTTP 403 when an authenticated user attempts an operation
     * they don't have permission for (e.g., editing another user's event).
     *
     * @param ex The caught ForbiddenException
     * @return HTTP 403 with error details
     */
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException): ResponseEntity<ErrorResponse> {
        logger.warning("Forbidden: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse("FORBIDDEN", ex.message ?: "Forbidden"))
    }

    /**
     * Handles missing resources (events, users, etc. not found in database).
     *
     * Returns HTTP 404 when a requested resource doesn't exist in Firestore.
     * This indicates the client requested an invalid ID or the resource was deleted.
     *
     * @param ex The caught ResourceNotFoundException
     * @return HTTP 404 with error details
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warning("Resource not found: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("NOT_FOUND", ex.message ?: "Resource not found"))
    }

    /**
     * Handles Bean Validation failures on request bodies.
     *
     * Returns HTTP 400 when `@Valid` request parameters fail validation constraints
     * (e.g., @NotBlank, @Email, @Size annotations). Aggregates all field errors
     * into a single error message.
     *
     * @param ex The caught MethodArgumentNotValidException from Spring Validation
     * @return HTTP 400 with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        logger.warning("Validation error: $errors")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("VALIDATION_ERROR", errors))
    }

    /**
     * Handles illegal arguments and business logic violations.
     *
     * Returns HTTP 400 for invalid inputs that don't fit other exception types
     * (e.g., "Event not found", "Username already taken"). Services throw
     * IllegalArgumentException for business rule violations.
     *
     * @param ex The caught IllegalArgumentException
     * @return HTTP 400 with error details
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warning("Illegal argument: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("BAD_REQUEST", ex.message ?: "Invalid request"))
    }

    /**
     * Handles all uncaught exceptions as a catch-all safety net.
     *
     * Returns HTTP 500 for unexpected errors (database failures, network issues, bugs).
     * Logs the full stack trace for debugging but returns a generic message to
     * clients to avoid leaking implementation details.
     *
     * If this handler is frequently triggered, it indicates unhandled edge cases
     * that should have specific exception handlers.
     *
     * @param ex The caught exception
     * @return HTTP 500 with generic error message
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.severe("Unhandled exception: ${ex.message}")
        ex.printStackTrace()
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
    }
}
