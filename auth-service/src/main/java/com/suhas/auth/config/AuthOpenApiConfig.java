package com.suhas.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuthOpenApiConfig {
    @Bean
    public OpenApiCustomizer filterActuatorEndpoints() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                // Remove the base discovery endpoint
                openApi.getPaths().remove("/actuator");
                // Remove the wildcard component endpoint
                openApi.getPaths().remove("/actuator/health/**");
                // The main /actuator/health stays!
            }
        };
    }

    @Bean
    public GroupedOpenApi authActuatorApi() {
        return GroupedOpenApi.builder()
                .group("Auth-Actuator")
                // This manually selects the specific endpoint even if show-actuator is false
                .pathsToMatch("/actuator/health")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("Public-API")
                .pathsToMatch("/api/**") // Adjust to your actual API prefix
                .addOpenApiCustomizer(openApi -> openApi.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication")))
                .build();
    }
}