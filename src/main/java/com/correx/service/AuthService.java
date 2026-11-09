package com.correx.service;

import com.correx.dto.LoginRequest;
import com.correx.dto.LoginResponse;
import com.correx.dto.RegisterRequest;
import com.correx.entity.Professor;
import com.correx.enums.ProfessorRole;
import com.correx.exception.BusinessException;
import com.correx.exception.UnauthorizedException;
import com.correx.repository.ProfessorRepository;
import com.correx.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(ProfessorRepository professorRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        var professor = professorRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(request.senha(), professor.getSenha())) {
            throw new UnauthorizedException("Email ou senha inválidos");
        }

        String token = jwtService.generateToken(professor.getEmail());
        return new LoginResponse(token, professor.getNome(), professor.getEmail());
    }

    public LoginResponse register(RegisterRequest request) {
        if (professorRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        Professor professor = Professor.builder()
            .nome(request.nome())
            .email(request.email())
            .senha(passwordEncoder.encode(request.senha()))
            .role(ProfessorRole.PROFESSOR)
            .ativo(true)
            .build();

        professorRepository.save(professor);

        String token = jwtService.generateToken(professor.getEmail());
        return new LoginResponse(token, professor.getNome(), professor.getEmail());
    }
}
