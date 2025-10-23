package no.ntnu.prog2007.ihostapi.security

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
 * Firebase token validation filter
 * Intercepts requests to extract and verify JWT tokens
 * Sets up Spring Security authentication context for authenticated users
 */
@Component
class FirebaseTokenFilter(private val firebaseAuth: FirebaseAuth) : OncePerRequestFilter() {
    private val log = Logger.getLogger(FirebaseTokenFilter::class.java.name)

    /**
     * Filter requests to validate Firebase JWT tokens
     * Token should be in Authorization header as: "Bearer <token>"
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
                val decodedToken = firebaseAuth.verifyIdToken(token)

                // Create authentication with user UID
                val authentication = UsernamePasswordAuthenticationToken(
                    decodedToken.uid,
                    null,
                    emptyList()
                )

                // Set authentication in security context
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                log.warning("Token verification failed: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer <token>"
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}
