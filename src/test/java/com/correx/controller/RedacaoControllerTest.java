package com.correx.controller;

import com.correx.dto.*;
import com.correx.entity.Professor;
import com.correx.security.JwtAuthenticationFilter;
import com.correx.security.RateLimitFilter;
import com.correx.service.RedacaoService;
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

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RedacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RedacaoController Tests")
@WithMockUser(username = "professor@correx.com", roles = "PROFESSOR")
class RedacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedacaoService redacaoService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private RedacaoResponse redacaoResponse;
    private RedacaoResumoResponse redacaoResumoResponse;

    @BeforeEach
    void setUp() {
        Professor professor = Professor.builder()
                .id(1L)
                .email("professor@correx.com")
                .nome("Professor")
                .build();
        lenient().when(securityUtils.getProfessorAutenticado()).thenReturn(professor);

        CorrecaoResponse correcaoResponse = new CorrecaoResponse(
                1L,
                7.5,
                null,
                "Boa redação",
                null,
                Arrays.asList(new NotaCriterioResponse("Gramática", 30, 7.0)),
                Arrays.asList(new ErroResponse("Grammar", "a important", "an important"))
        );

        redacaoResponse = new RedacaoResponse(
                1L,
                "Texto da redação",
                null,
                "INGLÊS",
                "CORRIGIDA",
                "TEXTO",
                null,
                null,
                null,
                1L,
                "Maria Silva",
                "Inglês A2",
                correcaoResponse,
                LocalDateTime.now()
        );

        redacaoResumoResponse = new RedacaoResumoResponse(
                1L,
                "Maria Silva",
                "Inglês A2",
                7.5,
                "CORRIGIDA",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve enviar redação por texto com sucesso")
    void testEnviarTexto() throws Exception {
        RedacaoTextoRequest request = new RedacaoTextoRequest(
                "Texto da redação",
                "INGLÊS",
                1L,
                1L,
                null
        );

        when(redacaoService.enviarTexto(any(RedacaoTextoRequest.class), anyLong())).thenReturn(redacaoResponse);

        mockMvc.perform(post("/api/v1/redacoes/texto")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CORRIGIDA"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao enviar redação sem texto")
    void testEnviarTextoSemTexto() throws Exception {
        RedacaoTextoRequest request = new RedacaoTextoRequest(
                "",
                "INGLÊS",
                1L,
                1L,
                null
        );

        mockMvc.perform(post("/api/v1/redacoes/texto")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar redações com paginação")
    void testListarRedacoes() throws Exception {
        PageResponse<RedacaoResumoResponse> pageResponse = new PageResponse<>(
                Arrays.asList(redacaoResumoResponse),
                0,
                10,
                1,
                1
        );

        when(redacaoService.listarRedacoes(anyLong(), nullable(String.class), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/redacoes?page=0&size=10")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Deve buscar redação por ID")
    void testBuscarRedacaoPorId() throws Exception {
        when(redacaoService.buscarPorId(1L, 1L)).thenReturn(redacaoResponse);

        mockMvc.perform(get("/api/v1/redacoes/1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.textoOriginal").value("Texto da redação"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar redação inexistente")
    void testBuscarRedacaoInexistente() throws Exception {
        when(redacaoService.buscarPorId(999L, 1L))
                .thenThrow(new com.correx.exception.ResourceNotFoundException("Redação não encontrada"));

        mockMvc.perform(get("/api/v1/redacoes/999")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar redações por aluno")
    void testListarRedacoesPorAluno() throws Exception {
        PageResponse<RedacaoResumoResponse> pageResponse = new PageResponse<>(
                Arrays.asList(redacaoResumoResponse),
                0,
                10,
                1,
                1
        );

        when(redacaoService.listarPorAluno(eq(1L), eq(1L), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/redacoes/aluno/1?page=0&size=10")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Deve revisar redação com sucesso")
    void testRevisarRedacao() throws Exception {
        RevisaoRequest request = new RevisaoRequest("Texto editado", 8.0, "Feedback");
        RedacaoResponse response = new RedacaoResponse(
                1L,
                "Texto da redação",
                "Texto editado",
                "INGLÊS",
                "REVISADA",
                "TEXTO",
                null,
                null,
                null,
                1L,
                "Maria Silva",
                "Inglês A2",
                null,
                LocalDateTime.now()
        );

        when(redacaoService.revisarCorrecao(eq(1L), any(RevisaoRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/v1/redacoes/1/revisao")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVISADA"));
    }

    @Test
    @DisplayName("Deve enviar redação para aluno")
    void testEnviarRedacaoParaAluno() throws Exception {
        RedacaoResponse response = new RedacaoResponse(
                1L,
                "Texto da redação",
                null,
                "INGLÊS",
                "ENVIADA",
                "TEXTO",
                null,
                null,
                null,
                1L,
                "Maria Silva",
                "Inglês A2",
                null,
                LocalDateTime.now()
        );

        when(redacaoService.enviarAoAluno(1L, 1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/redacoes/1/enviar")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADA"));
    }
}
