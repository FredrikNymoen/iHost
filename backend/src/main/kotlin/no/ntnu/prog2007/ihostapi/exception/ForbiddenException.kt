package no.ntnu.prog2007.ihostapi.exception

/**
 * Exception indicating the authenticated user lacks permission for the requested operation.
 *
 * This exception maps to HTTP 403 Forbidden and is thrown when:
 * - User attempts to modify a resource they don't own (e.g., updating another user's profile)
 * - User tries to delete an event they didn't create
 * - User accepts/declines an invitation not directed to them
 * - User performs admin-only operations without proper role
 *
 * Unlike [UnauthorizedException] (401), this exception indicates the user IS authenticated
 * but lacks authorization for the specific action.
 *
 * @property message Description of why access was denied
 * @see GlobalExceptionHandler.handleForbiddenException for HTTP response mapping
 */
class ForbiddenException(
    message: String
) : RuntimeException(message)
