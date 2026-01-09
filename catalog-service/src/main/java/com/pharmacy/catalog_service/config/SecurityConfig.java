package com.pharmacy.catalog_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/catalog/categories/**").hasAnyRole("ADMIN","PHARMACIST","CASHIER","STOREKEEPER")
                        .requestMatchers("/catalog/medicines/search").hasAnyRole("ADMIN","PHARMACIST","CASHIER","STOREKEEPER")
                        .requestMatchers("/catalog/medicines/**").hasAnyRole("ADMIN","PHARMACIST","CASHIER","STOREKEEPER")
                        // ghi dữ liệu: chỉ ADMIN/PHARMACIST
                        .requestMatchers("/catalog/**").authenticated()

                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
