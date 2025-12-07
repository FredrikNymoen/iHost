package no.ntnu.prog2007.ihostapi.exception

/**
 * Exception indicating a requested resource does not exist in the database.
 *
 * This exception maps to HTTP 404 Not Found and is thrown when:
 * - Event ID doesn't exist in Firestore
 * - User UID has no corresponding Firestore document
 * - Friendship ID is invalid or was deleted
 * - EventUser relationship doesn't exist
 *
 * This is distinct from returning null/empty results for queries that
 * legitimately find nothing (e.g., a user with no friends).
 *
 * @property message Description of which resource was not found
 * @see GlobalExceptionHandler.handleResourceNotFoundException for HTTP response mapping
 */
class ResourceNotFoundException(
    message: String
) : RuntimeException(message)
