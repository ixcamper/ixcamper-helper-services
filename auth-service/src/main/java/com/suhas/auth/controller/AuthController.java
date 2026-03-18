package com.suhas.auth.controller;

import com.suhas.auth.dto.AuthRequest;
import com.suhas.auth.dto.AuthResponse;
import com.suhas.auth.entity.User;
import com.suhas.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<AuthResponse> getToken(@RequestBody AuthRequest authRequest) {
        String token = authService.loginAndGenerateToken(authRequest.getUsername(), authRequest.getPassword());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(authRequest.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        authService.validateToken(token);
        return "Token is valid";
    }

//    @GetMapping("/me")
//    public User getCurrentUser(@RequestHeader("loggedInUser") String username) {
//        if (username == null || username.isEmpty()) {
//            // This means the request likely bypassed the Gateway
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Direct access not allowed");
//        }
//        return authService.getUserByUsername(username);
//    }

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        // Spring Security automatically populates this from the JWT token
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return authService.getUserByUsername(userDetails.getUsername());
    }
}
