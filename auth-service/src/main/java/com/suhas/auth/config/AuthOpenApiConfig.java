package com.suhas.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuthOpenApiConfig {

    @Bean
    @Primary // This is the "Golden Ticket" that fixes the startup error
    public OpenAPI authServiceOpenAPI() { // Changed from customOpenAPI
        return new OpenAPI()
                .addServersItem(new Server().url("/auth").description("Gateway Routed Path"));
    }

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
}