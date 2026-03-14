package com.suhas.auth.controller;

import com.suhas.auth.dto.AuthRequest;
import com.suhas.auth.entity.User;
import com.suhas.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String addNewUser(@RequestBody User user) {
        return authService.saveUser(user);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest authRequest) {
        // Change this line to match the method name in your AuthService
        return authService.loginAndGenerateToken(authRequest.getUsername(), authRequest.getPassword());
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        authService.validateToken(token);
        return "Token is valid";
    }
}