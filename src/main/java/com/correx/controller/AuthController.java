package com.correx.controller;

import com.correx.dto.LoginRequest;
import com.correx.dto.LoginResponse;
import com.correx.dto.RegisterRequest;
import com.correx.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final boolean registrationEnabled;

    public AuthController(AuthService authService,
                          @Value("${app.registration.enabled:false}") boolean registrationEnabled) {
        this.authService = authService;
        this.registrationEnabled = registrationEnabled;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Piloto: cadastro público desligado em produção para não expor a chave da OpenAI a desconhecidos.
        if (!registrationEnabled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(authService.register(request));
    }
}
