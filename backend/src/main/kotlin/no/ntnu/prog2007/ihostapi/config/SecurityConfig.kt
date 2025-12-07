package no.ntnu.prog2007.ihostapi.config

import no.ntnu.prog2007.ihostapi.security.filter.FirebaseTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security configuration for the iHost API.
 *
 * Implements a stateless JWT-based authentication strategy using Firebase Auth tokens.
 * This approach is chosen over session-based auth because:
 * - Mobile clients don't maintain server-side sessions efficiently
 * - JWT tokens are self-contained and don't require server-side state
 * - Firebase handles token generation/refresh on the client side
 *
 * The security flow works as follows:
 * 1. Client authenticates with Firebase Auth and receives a JWT token
 * 2. Client includes token in Authorization header for API requests
 * 3. [FirebaseTokenFilter] validates the token and extracts the user UID
 * 4. UID is stored in SecurityContext for use by controllers/services
 *
 * @property firebaseTokenFilter Custom filter that validates Firebase JWT tokens
 * @see FirebaseTokenFilter for token validation implementation
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(private val firebaseTokenFilter: FirebaseTokenFilter) {

    /**
     * Configures the HTTP security filter chain.
     *
     * Security decisions explained:
     * - **CSRF disabled**: Not needed for stateless REST APIs using JWT tokens,
     *   as tokens must be explicitly included in headers (immune to CSRF attacks)
     * - **Stateless sessions**: Each request is independently authenticated via JWT,
     *   reducing server memory usage and enabling horizontal scaling
     * - **Public endpoints**: Registration and availability checks must be accessible
     *   before users have authentication tokens
     *
     * @param http The HttpSecurity builder to configure
     * @return Configured SecurityFilterChain
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/health").permitAll()
                    .requestMatchers("/api/users/register").permitAll()
                    .requestMatchers("/api/users/username-available/**").permitAll()
                    .requestMatchers("/api/users/email-available/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
