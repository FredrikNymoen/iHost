package no.ntnu.prog2007.ihostapi.exception

/**
 * Exception thrown when a requested resource is not found
 */
class ResourceNotFoundException(
    message: String
) : RuntimeException(message)
