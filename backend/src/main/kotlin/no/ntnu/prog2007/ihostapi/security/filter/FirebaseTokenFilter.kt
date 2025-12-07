package no.ntnu.prog2007.ihostapi.security.filter

import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.logging.Logger

/**
 * Spring Security filter that validates Firebase JWT tokens on each request.
 *
 * This filter is registered in the Spring Security filter chain (before
 * UsernamePasswordAuthenticationFilter) and executes once per request to:
 * 1. Extract JWT token from the Authorization header
 * 2. Verify the token's signature and claims with Firebase Auth
 * 3. Populate SecurityContext with the authenticated user's UID
 *
 * Authentication flow:
 * - Mobile app authenticates with Firebase Auth (email/password)
 * - Firebase issues a JWT token to the client
 * - Client includes token in Authorization header: `Bearer <jwt-token>`
 * - This filter verifies the token and extracts the user's UID
 * - Controllers access the UID via `SecurityContextHolder.getContext().authentication.principal`
 *
 * Public endpoints (configured in SecurityConfig) skip this filter's authentication requirement
 * but still execute the filter (token validation happens but doesn't block access).
 *
 * @property firebaseAuth Firebase Admin SDK auth instance for token verification
 * @see no.ntnu.prog2007.ihostapi.config.SecurityConfig for filter registration
 * @see no.ntnu.prog2007.ihostapi.config.FirebaseConfig for FirebaseAuth bean
 */
@Component
class FirebaseTokenFilter(private val firebaseAuth: FirebaseAuth) : OncePerRequestFilter() {

    /**
     * Validates Firebase JWT tokens and establishes authentication context.
     *
     * Token verification checks:
     * - Signature validity (using Firebase project's secret key)
     * - Expiration timestamp (tokens expire after 1 hour by default)
     * - Audience and issuer claims match the Firebase project
     * - Token hasn't been revoked
     *
     * @param request The HTTP request to filter
     * @param response The HTTP response (may send 401 if token invalid)
     * @param filterChain The filter chain to continue execution
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null) {
            try {
                // Verify token with Firebase
                val decodedToken = firebaseAuth.verifyIdToken(token, true)

                // Create authentication with user UID
                val authentication = UsernamePasswordAuthenticationToken(
                    decodedToken.uid,
                    null,
                    emptyList()
                )

                // Set authentication in security context
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * Expected header format: `Authorization: Bearer <jwt-token>`
     *
     * The "Bearer" prefix is required per OAuth 2.0 Bearer Token standard (RFC 6750).
     * Returns null if:
     * - Authorization header is missing
     * - Header doesn't start with "Bearer "
     * - Header is malformed
     *
     * @param request The HTTP request containing headers
     * @return The extracted JWT token string, or null if not present/invalid format
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}