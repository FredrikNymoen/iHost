package no.ntnu.prog2007.ihostapi.exception

/**
 * Exception thrown when a user does not have permission to access a resource
 */
class ForbiddenException(
    message: String
) : RuntimeException(message)
