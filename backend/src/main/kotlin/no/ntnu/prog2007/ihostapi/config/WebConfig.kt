package no.ntnu.prog2007.ihostapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC configuration for cross-origin request handling.
 *
 * Configures CORS (Cross-Origin Resource Sharing) to allow the API to be accessed
 * from different origins. This is necessary because:
 * - Mobile apps may run on different domains/ports during development
 * - The Android emulator uses a different origin than the backend server
 *
 * **Security Note**: The current configuration allows all origins (`*`) for development
 * flexibility. In production, this should be restricted to known client origins
 * to prevent unauthorized cross-origin access.
 *
 * Configuration details:
 * - `allowedOrigins("*")`: Permits requests from any origin
 * - `allowedMethods`: Restricts to standard REST methods (no PATCH/OPTIONS explicitly)
 * - `allowedHeaders("*")`: Accepts all headers including Authorization for JWT tokens
 */

@Configuration
class WebConfig : WebMvcConfigurer {


    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
    }
}