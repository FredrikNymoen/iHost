package no.ntnu.prog2007.ihostapi.config

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Cloudinary image service configuration.
 *
 * Cloudinary is used as the image storage and CDN solution because:
 * - **Automatic optimization**: Images are transformed and compressed on-the-fly
 * - **Global CDN**: Fast delivery to users regardless of location
 * - **No storage management**: Eliminates need for self-hosted file storage
 * - **URL-based transformations**: Resize, crop, and format images via URL parameters
 *
 * Credentials are injected from `application.properties` or environment variables,
 * keeping sensitive API keys out of source code.
 *
 * @see no.ntnu.prog2007.ihostapi.service.impl.CloudinaryServiceImpl for upload implementation
 */
@Configuration
class CloudinaryConfig {

    /** Cloudinary cloud name identifying the account */
    @Value("\${cloudinary.cloud-name}")
    private lateinit var cloudName: String

    /** API key for Cloudinary authentication */
    @Value("\${cloudinary.api-key}")
    private lateinit var apiKey: String

    /** API secret for signed requests (never expose to clients) */
    @Value("\${cloudinary.api-secret}")
    private lateinit var apiSecret: String

    /**
     * Creates the Cloudinary client instance.
     *
     * The client is configured with account credentials and used by
     * [CloudinaryServiceImpl] to upload images for events and user profiles.
     *
     * @return Configured Cloudinary client ready for API operations
     */
    @Bean
    fun cloudinary(): Cloudinary {
        val config = mapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret
        )
        return Cloudinary(config)
    }
}