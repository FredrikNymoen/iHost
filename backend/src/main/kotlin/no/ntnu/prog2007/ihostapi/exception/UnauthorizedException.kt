package no.ntnu.prog2007.ihostapi.exception

/**
 * Exception thrown when a user is not authenticated or token is invalid
 */
class UnauthorizedException(
    message: String
) : RuntimeException(message)
