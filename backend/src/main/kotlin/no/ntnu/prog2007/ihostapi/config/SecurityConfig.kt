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
 * Spring Security configuration
 * Configures authentication, authorization, and the Firebase token filter
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(private val firebaseTokenFilter: FirebaseTokenFilter) {

    /**
     * Configure security filter chain
     * - Disable CSRF (using JWT tokens instead)
     * - Use stateless session management (JWT-based)
     * - Define public and protected endpoints
     * - Register Firebase token filter
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Disable CSRF since we use JWT tokens
            .csrf { it.disable() }
            // Use stateless sessions (JWT-based authentication)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // Define which endpoints require authentication
            .authorizeHttpRequests { authz ->
                authz
                    // Public endpoints
                    .requestMatchers("/health").permitAll()
                    .requestMatchers("/api/users/register").permitAll()
                    .requestMatchers("/api/users/username-available/**").permitAll()
                    .requestMatchers("/api/users/email-available/**").permitAll()
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }
            // Add Firebase token filter before default authentication filter
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
