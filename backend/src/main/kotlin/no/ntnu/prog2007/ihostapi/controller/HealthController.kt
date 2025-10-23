package no.ntnu.prog2007.ihostapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Health check controller
 * Public endpoint for monitoring API health status
 */
@RestController
@RequestMapping("/health")
class HealthController {

    /**
     * Check if API is running
     */
    @GetMapping
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf("status" to "UP")
        )
    }
}
