package com.correx.controller;

import com.correx.dto.TurmaRequest;
import com.correx.dto.TurmaResponse;
import com.correx.entity.Professor;
import com.correx.security.JwtAuthenticationFilter;
import com.correx.security.RateLimitFilter;
import com.correx.service.TurmaService;
import com.correx.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TurmaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TurmaController Tests")
@WithMockUser(username = "professor@correx.com", roles = "PROFESSOR")
class TurmaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TurmaService turmaService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private TurmaResponse turmaResponse;

    @BeforeEach
    void setUp() {
        Professor professor = Professor.builder()
                .id(1L)
                .email("professor@correx.com")
                .nome("Professor")
                .build();
        lenient().when(securityUtils.getProfessorAutenticado()).thenReturn(professor);

        turmaResponse = new TurmaResponse(1L, "Inglês A2", true, 10, 7.5);
    }

    @Test
    @DisplayName("Deve listar turmas do professor")
    void testListarTurmas() throws Exception {
        List<TurmaResponse> turmas = Arrays.asList(turmaResponse);
        when(turmaService.listarTurmas(1L)).thenReturn(turmas);
        mockMvc.perform(get("/api/v1/turmas")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("Deve buscar turma por ID")
    void testBuscarTurmaPorId() throws Exception {
        when(turmaService.buscarPorId(1L, 1L)).thenReturn(turmaResponse);

        mockMvc.perform(get("/api/v1/turmas/1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Inglês A2"))
                .andExpect(jsonPath("$.ativa").value(true));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar turma inexistente")
    void testBuscarTurmaInexistente() throws Exception {
        when(turmaService.buscarPorId(999L, 1L))
                .thenThrow(new com.correx.exception.ResourceNotFoundException("Turma não encontrada"));

        mockMvc.perform(get("/api/v1/turmas/999")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Deve criar turma com sucesso")
    void testCriarTurma() throws Exception {
        TurmaRequest request = new TurmaRequest("Espanhol B1", true);
        TurmaResponse response = new TurmaResponse(2L, "Espanhol B1", true, 0, 0.0);

        when(turmaService.criar(any(TurmaRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(post("/api/v1/turmas")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Espanhol B1"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar turma sem nome")
    void testCriarTurmaSemNome() throws Exception {
        TurmaRequest request = new TurmaRequest("", true);

        mockMvc.perform(post("/api/v1/turmas")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Deve atualizar turma com sucesso")
    void testAtualizarTurma() throws Exception {
        TurmaRequest request = new TurmaRequest("Inglês A2 Avançado", false);
        TurmaResponse response = new TurmaResponse(1L, "Inglês A2 Avançado", false, 10, 7.5);

        when(turmaService.atualizar(eq(1L), any(TurmaRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/v1/turmas/1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Inglês A2 Avançado"))
                .andExpect(jsonPath("$.ativa").value(false));
    }

    @Test
    @DisplayName("Deve deletar turma com sucesso")
    void testDeletarTurma() throws Exception {
        doNothing().when(turmaService).deletar(eq(1L), anyLong());

        mockMvc.perform(delete("/api/v1/turmas/1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(turmaService, times(1)).deletar(1L, 1L);
    }
}
