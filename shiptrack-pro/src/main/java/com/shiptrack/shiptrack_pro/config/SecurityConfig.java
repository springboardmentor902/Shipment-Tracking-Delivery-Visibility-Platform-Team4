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

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175",
                "http://localhost:5176",
                "http://localhost:5177",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ===== PUBLIC ENDPOINTS =====
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/ws/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // ===== USER MANAGEMENT =====
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/{id}/status")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")

                        // ===== ADMIN =====
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRATOR")

                        // ===== BUSINESS ACCOUNT =====
                        .requestMatchers("/api/business/account/me").authenticated()
                        .requestMatchers("/api/business/account/check").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/business/account").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/business/account/me").authenticated()
                        .requestMatchers("/api/business/accounts/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/business/account/user/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/business/account/*/verify").hasRole("ADMINISTRATOR")

                        // ===== SHIPMENT - FIXED =====
                        // ✅ Customers can view their own shipments
                        .requestMatchers(HttpMethod.GET, "/api/shipments/user").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/shipments/user/**").authenticated()

                        // ✅ Anyone can view by ID or tracking number
                        .requestMatchers(HttpMethod.GET, "/api/shipments/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/shipments/tracking/{trackingNumber}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/shipments/search").authenticated()

                        // ✅ Business Client, Operator, Admin can create shipments
                        .requestMatchers(HttpMethod.POST, "/api/shipments")
                        .hasAnyRole("BUSINESS_CLIENT", "LOGISTICS_OPERATOR", "ADMINISTRATOR")

                        // ✅ View all shipments - Operator, Admin
                        .requestMatchers(HttpMethod.GET, "/api/shipments")
                        .hasAnyRole("BUSINESS_CLIENT", "LOGISTICS_OPERATOR", "ADMINISTRATOR")

                        // ✅ Active shipments - anyone authenticated
                        .requestMatchers(HttpMethod.GET, "/api/shipments/active").authenticated()

                        // ✅ Update - Operator/Admin
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/{id}")
                        .hasAnyRole("BUSINESS_CLIENT", "LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/shipments/{id}/status")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/shipments/{id}/cancel")
                        .hasAnyRole("BUSINESS_CLIENT", "LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/shipments/{id}")
                        .hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/delayed")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/stats/**")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")

                        // ===== PACKAGE =====
                        .requestMatchers(HttpMethod.GET, "/api/packages/shipment/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/packages/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/packages")
                        .hasAnyRole("BUSINESS_CLIENT", "LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/packages/**")
                        .hasAnyRole("BUSINESS_CLIENT", "LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/packages/**")
                        .hasAnyRole("BUSINESS_CLIENT", "LOGISTICS_OPERATOR", "ADMINISTRATOR")

                        // ===== ROUTE =====
                        .requestMatchers(HttpMethod.POST, "/api/routes")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/routes/shipment/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/routes/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/routes")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/routes/driver/**")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/routes/{id}")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/routes/{id}/driver")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/routes/{id}/status")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/routes/{id}")
                        .hasRole("ADMINISTRATOR")

                        // ===== LOCATION =====
                        .requestMatchers(HttpMethod.POST, "/api/location/route/**")
                        .hasAnyRole("LOGISTICS_OPERATOR", "DRIVER", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/location/shipment/**").authenticated()

                        // ===== TRACKING =====
                        .requestMatchers(HttpMethod.POST, "/api/tracking/{shipmentId}/events")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/tracking/{shipmentId}/history").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tracking/{shipmentId}/latest").authenticated()

                        // ===== GEOCODING =====
                        .requestMatchers("/api/geocode/**").authenticated()

                        // ===== PROOF OF DELIVERY =====
                        .requestMatchers(HttpMethod.POST, "/api/pod/**")
                        .hasAnyRole("LOGISTICS_OPERATOR", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/pod/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/pod/*/verify")
                        .hasRole("ADMINISTRATOR")

                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}