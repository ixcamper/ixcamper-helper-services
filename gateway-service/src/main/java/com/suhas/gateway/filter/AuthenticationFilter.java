package com.suhas.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import com.suhas.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j // This provides the 'log' variable
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                // Check if header contains token
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return handleUnAuthorized(exchange, "Missing Authorization Header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(authHeader);

                    // If valid, pass the username header to downstream
                    String username = jwtUtil.extractUsername(authHeader);
                    exchange.getRequest().mutate()
                            .header("loggedInUser", username)
                            .build();

                } catch (io.jsonwebtoken.ExpiredJwtException e) {
                    log.error("JWT Token has expired: {}", e.getMessage()); // Logs to console
                    return handleUnAuthorized(exchange, "Token Expired. Please login again.");
                } catch (io.jsonwebtoken.security.SignatureException e) {
                    log.error("Invalid JWT Signature: {}", e.getMessage()); // Logs to console
                    return handleUnAuthorized(exchange, "Invalid Token Signature.");
                } catch (Exception e) {
                    log.error("Authentication failed: {}", e.getMessage()); // Logs any other error
                    return handleUnAuthorized(exchange, "Invalid Access...!");
                }
            }
            return chain.filter(exchange);
        });
    }

    private Mono<Void> handleUnAuthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Create a JSON response body
        String responseBody = "{\"message\": \"" + message + "\"}";

        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // You can add configuration properties here if needed
    }
}