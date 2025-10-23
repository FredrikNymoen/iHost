package no.ntnu.prog2007.ihostapi.model

/**
 * Standard error response for API endpoints
 */
data class ErrorResponse(
    val error: String,
    val message: String
)