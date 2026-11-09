package com.correx.controller;

import com.correx.dto.LoginRequest;
import com.correx.dto.LoginResponse;
import com.correx.dto.RegisterRequest;
import com.correx.security.JwtAuthenticationFilter;
import com.correx.security.RateLimitFilter;
import com.correx.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("professor@correx.com", "123456");
        registerRequest = new RegisterRequest("Novo Professor", "novo@correx.com", "senha123");
        loginResponse = new LoginResponse("jwt-token-xyz", "Professor Demo", "professor@correx.com");
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void testLoginSuccess() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(loginResponse);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.nome").value("Professor Demo"))
                .andExpect(jsonPath("$.email").value("professor@correx.com"))
                .andReturn();
    }

    @Test
    @DisplayName("Deve retornar 400 ao fazer login com email inválido")
    void testLoginWithInvalidEmail() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("invalid-email", "123456");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Deve registrar novo professor com sucesso")
    void testRegisterSuccess() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new LoginResponse("jwt-token-novo", "Novo Professor", "novo@correx.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.nome").value("Novo Professor"))
                .andExpect(jsonPath("$.email").value("novo@correx.com"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao registrar com senha muito curta")
    void testRegisterWithShortPassword() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("Test", "test@correx.com", "123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao registrar sem nome")
    void testRegisterWithoutName() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "test@correx.com", "senha123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
