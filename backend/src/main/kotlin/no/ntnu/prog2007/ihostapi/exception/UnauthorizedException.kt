package no.ntnu.prog2007.ihostapi.exception

/**
 * Exception indicating authentication failure or invalid credentials.
 *
 * This exception maps to HTTP 401 Unauthorized and is thrown when:
 * - No Authorization header is present in the request
 * - Firebase JWT token is expired, malformed, or has invalid signature
 * - Token verification fails in [FirebaseTokenFilter]
 * - SecurityContext doesn't contain authenticated user principal
 *
 * Mobile clients should respond by re-authenticating with Firebase Auth
 * and obtaining a fresh JWT token.
 *
 * @property message Description of the authentication failure
 * @see GlobalExceptionHandler.handleUnauthorizedException for HTTP response mapping
 * @see no.ntnu.prog2007.ihostapi.security.filter.FirebaseTokenFilter for token validation
 */
class UnauthorizedException(
    message: String
) : RuntimeException(message)
