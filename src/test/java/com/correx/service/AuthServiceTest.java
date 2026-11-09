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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Professor professor;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        professor = new Professor();
        professor.setId(1L);
        professor.setNome("Professor Demo");
        professor.setEmail("professor@correx.com");
        professor.setSenha("$2a$10$encrypted.password.here");
        professor.setRole(ProfessorRole.PROFESSOR);
        professor.setAtivo(true);

        loginRequest = new LoginRequest("professor@correx.com", "123456");
        registerRequest = new RegisterRequest("Novo Professor", "novo@correx.com", "senha123");
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void testLoginSuccess() {
        when(professorRepository.findByEmail("professor@correx.com"))
                .thenReturn(Optional.of(professor));
        when(passwordEncoder.matches("123456", professor.getSenha()))
                .thenReturn(true);
        when(jwtService.generateToken("professor@correx.com"))
                .thenReturn("jwt-token-xyz");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token-xyz", response.token());
        assertEquals("Professor Demo", response.nome());
        assertEquals("professor@correx.com", response.email());
        verify(professorRepository, times(1)).findByEmail("professor@correx.com");
        verify(passwordEncoder, times(1)).matches("123456", professor.getSenha());
        verify(jwtService, times(1)).generateToken("professor@correx.com");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException com email inválido")
    void testLoginWithInvalidEmail() {
        when(professorRepository.findByEmail("invalid@correx.com"))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(new LoginRequest("invalid@correx.com", "123456"));
        });

        verify(professorRepository, times(1)).findByEmail("invalid@correx.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException com senha inválida")
    void testLoginWithInvalidPassword() {
        when(professorRepository.findByEmail("professor@correx.com"))
                .thenReturn(Optional.of(professor));
        when(passwordEncoder.matches("senha-errada", professor.getSenha()))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(new LoginRequest("professor@correx.com", "senha-errada"));
        });

        verify(professorRepository, times(1)).findByEmail("professor@correx.com");
        verify(passwordEncoder, times(1)).matches("senha-errada", professor.getSenha());
    }

    @Test
    @DisplayName("Deve registrar novo professor com sucesso")
    void testRegisterSuccess() {
        when(professorRepository.existsByEmail("novo@correx.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("senha123"))
                .thenReturn("$2a$10$hashed.password");
        when(professorRepository.save(any(Professor.class)))
                .thenAnswer(invocation -> {
                    Professor p = invocation.getArgument(0);
                    p.setId(2L);
                    return p;
                });
        when(jwtService.generateToken("novo@correx.com"))
                .thenReturn("jwt-token-novo");

        LoginResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token-novo", response.token());
        assertEquals("Novo Professor", response.nome());
        assertEquals("novo@correx.com", response.email());
        verify(professorRepository, times(1)).existsByEmail("novo@correx.com");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(professorRepository, times(1)).save(any(Professor.class));
        verify(jwtService, times(1)).generateToken("novo@correx.com");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao registrar email duplicado")
    void testRegisterWithDuplicateEmail() {
        when(professorRepository.existsByEmail("novo@correx.com"))
                .thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        verify(professorRepository, times(1)).existsByEmail("novo@correx.com");
        verify(professorRepository, never()).save(any(Professor.class));
    }

    @Test
    @DisplayName("Deve criptografar senha ao registrar")
    void testRegisterEncryptsPassword() {
        when(professorRepository.existsByEmail("novo@correx.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("senha123"))
                .thenReturn("$2a$10$encrypted");
        when(professorRepository.save(any(Professor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyString()))
                .thenReturn("token");

        authService.register(registerRequest);

        verify(passwordEncoder, times(1)).encode("senha123");
    }
}
