package no.ntnu.prog2007.ihostapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Health check controller providing API status monitoring.
 *
 * This controller exposes a public endpoint for infrastructure monitoring
 * and health checks without requiring authentication. It's commonly used by:
 * - Load balancers to verify server availability
 * - Monitoring systems to detect service outages
 * - CI/CD pipelines to confirm successful deployments
 *
 * The endpoint is explicitly permitted in [SecurityConfig] and doesn't require JWT tokens.
 *
 * @see no.ntnu.prog2007.ihostapi.config.SecurityConfig for public endpoint configuration
 */
@RestController
@RequestMapping("/health")
class HealthController {

    /**
     * Returns the health status of the API.
     *
     * This is a simple liveness check that returns HTTP 200 with status "UP"
     * if the Spring application context is running. It doesn't verify:
     * - Database connectivity (Firestore)
     * - External service availability (Firebase Auth, Cloudinary)
     * - System resource availability
     *
     * @return ResponseEntity with status "UP" and HTTP 200 OK
     */
    @GetMapping
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf("status" to "UP")
        )
    }
}
