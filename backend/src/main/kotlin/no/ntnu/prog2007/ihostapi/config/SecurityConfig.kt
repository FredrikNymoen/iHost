package org.example.noteappapi.config

import org.example.noteappapi.security.FirebaseTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val firebaseTokenFilter: FirebaseTokenFilter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/auth/verify").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
