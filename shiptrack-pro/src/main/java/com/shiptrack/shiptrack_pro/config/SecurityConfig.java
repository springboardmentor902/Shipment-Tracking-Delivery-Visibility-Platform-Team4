package com.shiptrack.shiptrack_pro.config;

import com.shiptrack.shiptrack_pro.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf -> csrf.disable())

                // =================================================
                // CORS
                // =================================================

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // =================================================
                // STATELESS SESSION
                // =================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()

                        // -------------------------------------------------
                        // AUTHENTICATION
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()


                                // -------------------------------------------------
                                // SHIPMENTS
                                 // -------------------------------------------------

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/shipments"
                                ).hasAnyRole(
                                        "CUSTOMER",
                                        "BUSINESS_CLIENT"
                                )

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/shipments",
                                        "/api/shipments/**"
                                ).hasAnyRole(
                                        "CUSTOMER",
                                        "BUSINESS_CLIENT",
                                        "LOGISTICS_OPERATOR",
                                        "ADMINISTRATOR"
                                )

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/shipments/**"
                                ).hasAnyRole(
                                        "CUSTOMER",
                                        "BUSINESS_CLIENT",
                                        "LOGISTICS_OPERATOR",
                                        "ADMINISTRATOR"
                                )

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/shipments/**"
                                ).hasAnyRole(
                                        "LOGISTICS_OPERATOR",
                                        "ADMINISTRATOR"
                                )

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/shipments/**"
                                ).hasAnyRole(
                                        "CUSTOMER",
                                        "BUSINESS_CLIENT",
                                        "LOGISTICS_OPERATOR",
                                        "ADMINISTRATOR"
                                )


                        // -------------------------------------------------
                        // BUSINESS ACCOUNT
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/business_acc/**"
                        ).hasRole(
                                "BUSINESS_CLIENT"
                        )


                                // -------------------------------------------------
// TRACKING
// -------------------------------------------------

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/tracking/shipments/*/events"
                                ).hasAnyRole(
                                        "BUSINESS_CLIENT",
                                        "LOGISTICS_OPERATOR",
                                        "ADMINISTRATOR"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/tracking/shipments/*/events"
                                ).hasAnyRole(
                                        "LOGISTICS_OPERATOR",
                                        "ADMINISTRATOR"
                                )

                                .requestMatchers(
                                        "/api/tracking/**"
                                ).hasAnyRole(
                                        "LOGISTICS_OPERATOR",
                                        "ADMINISTRATOR"
                                )


                        // -------------------------------------------------
                        // ROUTES
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/routes/**"
                        ).hasAnyRole(
                                "LOGISTICS_OPERATOR",
                                "ADMINISTRATOR"
                        )


                        // -------------------------------------------------
                        // PROOF OF DELIVERY
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pod/**"
                        ).hasRole(
                                "LOGISTICS_OPERATOR"
                        )

                        .requestMatchers(
                                "/api/pod/**"
                        ).hasAnyRole(
                                "LOGISTICS_OPERATOR",
                                "ADMINISTRATOR"
                        )


                        // -------------------------------------------------
                        // ANALYTICS
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/analytics/**"
                        ).hasAnyRole(
                                "BUSINESS_CLIENT",
                                "ADMINISTRATOR"
                        )


                        // -------------------------------------------------
                        // REPORTS
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/reports/**"
                        ).hasAnyRole(
                                "BUSINESS_CLIENT",
                                "ADMINISTRATOR"
                        )


                        // -------------------------------------------------
                        // ADMIN
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole(
                                "ADMINISTRATOR"
                        )


                        // -------------------------------------------------
                        // USER STATUS
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/users/*/status"
                        ).hasAnyRole(
                                "LOGISTICS_OPERATOR",
                                "ADMINISTRATOR"
                        )


                        // -------------------------------------------------
                        // USER APIs
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/users/**"
                        ).authenticated()


                        // -------------------------------------------------
                        // EVERYTHING ELSE
                        // -------------------------------------------------

                        .anyRequest().authenticated()
                )

                // =================================================
                // DISABLE BASIC AUTH
                // =================================================

                .httpBasic(basic ->
                        basic.disable()
                )

                // =================================================
                // DISABLE FORM LOGIN
                // =================================================

                .formLogin(form ->
                        form.disable()
                )

                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}