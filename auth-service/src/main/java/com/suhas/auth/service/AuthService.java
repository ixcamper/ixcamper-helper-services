package com.suhas.auth.service;

import com.suhas.auth.entity.User;
import com.suhas.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    // 1. Inject the AuthenticationManager
    private final AuthenticationManager authenticationManager;

    public String saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        return "User registered successfully";
    }

    public String loginAndGenerateToken(String username, String password) {
        // 2. This is the crucial step! This line checks the raw password
        // against the hashed password in Neon via your UserDetailsService.
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 3. Only if authentication is successful do we generate the token
        if (authenticate.isAuthenticated()) {
            return jwtService.generateToken(username);
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }

    public void validateToken(String token) {
        jwtService.validateToken(token); // Ensure your jwtService has a validation method
    }
}