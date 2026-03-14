package com.suhas.gateway.filter;

import com.suhas.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
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
            // 1. Check if the route is secured (not /auth/register or /auth/token)
            if (validator.isSecured.test(exchange.getRequest())) {

                // 2. Check if the header contains the Authorization entry
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // 3. Validate the token
                    jwtUtil.validateToken(authHeader);
                } catch (Exception e) {
                    System.out.println("Invalid Access...!");
                    throw new RuntimeException("Unauthorized access to application");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
        // You can add configuration properties here if needed
    }
}