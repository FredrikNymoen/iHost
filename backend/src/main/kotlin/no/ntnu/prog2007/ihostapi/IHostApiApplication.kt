package no.ntnu.prog2007.ihostapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * iHostAPI application entry point
 * REST API backend for iHost mobile application
 */
@SpringBootApplication
class IHostApiApplication

/**
 * Application main function
 * Starts the Spring Boot application
 */
fun main(args: Array<String>) {
    runApplication<IHostApiApplication>(*args)
}
