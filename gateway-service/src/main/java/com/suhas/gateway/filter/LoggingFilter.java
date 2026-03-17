package com.suhas.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Pre-filter: Log the incoming request
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().name();

        logger.info("Incoming Request: {} {}", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Post-filter: Log the response status
            int statusCode = exchange.getResponse().getStatusCode().value();
            logger.info("Outgoing Response for {}: Status Code {}", path, statusCode);
        }));
    }

    @Override
    public int getOrder() {
        // Set priority. -1 ensures it runs before security/other filters.
        return -1;
    }
}