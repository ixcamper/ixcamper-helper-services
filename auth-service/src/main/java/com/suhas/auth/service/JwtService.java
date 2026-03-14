package com.suhas.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; // Import this
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Injecting the value from application.yml
    @Value("${jwt.secret}")
    private String secret;

    public void validateToken(final String token) {
        Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token);
    }

    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        // Use the injected 'secret' variable instead of the hardcoded constant
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}