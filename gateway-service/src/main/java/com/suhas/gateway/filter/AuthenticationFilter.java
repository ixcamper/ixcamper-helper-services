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
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
                // 1. Check for Header
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(authHeader);

                    // 2. Extract and Pass Header to Downstream
                    String username = jwtUtil.extractUsername(authHeader);

                    // Note: mutate() requires rebuilding the exchange or using the mutated request
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("loggedInUser", username)
                            .build();

                    return chain.filter(exchange.mutate().request(request).build());

                } catch (io.jsonwebtoken.ExpiredJwtException e) {
                    log.error("JWT Token has expired: {}", e.getMessage()); // Logs to console
                    return onError(exchange, "Token Expired. Please login again.", HttpStatus.UNAUTHORIZED);
                } catch (io.jsonwebtoken.security.SignatureException e) {
                    log.error("Invalid JWT Signature: {}", e.getMessage()); // Logs to console
                    return onError(exchange, "Invalid Token Signature.", HttpStatus.UNAUTHORIZED);
                } catch (Exception e) {
                    log.error("Authentication failed: {}", e.getMessage()); // Logs any other error
                    return onError(exchange, "Unauthorized access to this resource.", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        });
    }

    // Unified Error Method to match Common Module ErrorResponse DTO
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Matches your common.dto.ErrorResponse structure exactly
        String jsonResponse = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                err,
                exchange.getRequest().getPath()
        );

        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // You can add configuration properties here if needed
    }
}