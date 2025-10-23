package no.ntnu.prog2007.ihostapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web configuration
 * Configures CORS (Cross-Origin Resource Sharing) for API access from mobile/frontend clients
 */
@Configuration
class WebConfig : WebMvcConfigurer {
    /**
     * Configure CORS mappings to allow requests from all origins
     * Allow common HTTP methods and all headers for flexibility
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
    }
}