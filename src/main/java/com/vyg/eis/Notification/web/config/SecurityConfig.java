package com.vyg.eis.Notification.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/public/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())) // Custom JWT
                                                                                                  // Converter
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractRoles);
        return converter;
    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {

    // Extract the resource_access map
    Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
    System.out.println("resourceAccess" + jwt.getClaims().get("aud"));
    System.out.println("resourceAccess JWT" + jwt);
    System.out.println("resourceAccessR" + jwt.getClaims());

    // Get the list of clients (client IDs)
    List<String> clients = (List<String>) jwt.getClaims().get("aud");
    System.out.println("clients" + clients);

    // Initialize a list to store all roles
    List<String> roles = new ArrayList<>();

    // Loop through each client and extract roles
    for (String client : clients) {
        if (resourceAccess.containsKey(client)) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(client);
            System.out.println("clientAccess for " + client + ": " + clientAccess);
            List<String> clientRoles = (List<String>) clientAccess.get("roles");
            if (clientRoles != null) {
                roles.addAll(clientRoles); // Add roles for the client
            }
        }
    }

    System.out.println("roles"+roles);

    // Convert roles to Spring Security authorities
    return roles.stream()
            .map(role -> "ROLE_" + role) // Prefix with ROLE_ for Spring Security
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
}

}
