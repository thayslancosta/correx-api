package com.correx.controller;

import com.correx.dto.CriterioRequest;
import com.correx.dto.CriterioResponse;
import com.correx.dto.ValidacaoCriteriosResponse;
import com.correx.entity.Professor;
import com.correx.security.JwtAuthenticationFilter;
import com.correx.security.RateLimitFilter;
import com.correx.service.CriterioService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CriterioController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CriterioController Tests")
@WithMockUser(username = "professor@correx.com", roles = "PROFESSOR")
class CriterioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CriterioService criterioService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private CriterioResponse criterioResponse;

    @BeforeEach
    void setUp() {
        Professor professor = Professor.builder()
                .id(1L)
                .email("professor@correx.com")
                .nome("Professor")
                .build();
        lenient().when(securityUtils.getProfessorAutenticado()).thenReturn(professor);

        criterioResponse = new CriterioResponse(1L, "Gramática", "Avaliação de gramática", 30, true);
    }

    @Test
    @DisplayName("Deve listar critérios do professor")
    void testListarCriterios() throws Exception {
        when(criterioService.listarCriterios(anyLong())).thenReturn(Arrays.asList(criterioResponse));

        mockMvc.perform(get("/api/v1/criterios")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Deve criar critério com sucesso")
    void testCriarCriterio() throws Exception {
        CriterioRequest request = new CriterioRequest("Ortografia", "Avaliação de ortografia", 25, true);
        CriterioResponse response = new CriterioResponse(2L, "Ortografia", "Avaliação de ortografia", 25, true);

        when(criterioService.criar(any(CriterioRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(post("/api/v1/criterios")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Ortografia"))
                .andExpect(jsonPath("$.peso").value(25));
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar critério com peso inválido")
    void testCriarCriterioComPesoInvalido() throws Exception {
        CriterioRequest request = new CriterioRequest("Teste", "Descrição", 101, true);

        mockMvc.perform(post("/api/v1/criterios")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar critério com peso zero")
    void testCriarCriterioComPesoZero() throws Exception {
        CriterioRequest request = new CriterioRequest("Teste", "Descrição", 0, true);

        mockMvc.perform(post("/api/v1/criterios")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve atualizar critério com sucesso")
    void testAtualizarCriterio() throws Exception {
        CriterioRequest request = new CriterioRequest("Gramática Avançada", "Descrição", 30, false);
        CriterioResponse response = new CriterioResponse(1L, "Gramática Avançada", "Descrição", 30, false);

        when(criterioService.atualizar(eq(1L), any(CriterioRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/v1/criterios/1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gramática Avançada"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    @DisplayName("Deve deletar critério com sucesso")
    void testDeletarCriterio() throws Exception {
        doNothing().when(criterioService).deletar(eq(1L), anyLong());

        mockMvc.perform(delete("/api/v1/criterios/1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve validar critérios com soma igual a 100")
    void testValidarCritériosSoma100() throws Exception {
        ValidacaoCriteriosResponse validacao = new ValidacaoCriteriosResponse(true, 100);

        when(criterioService.validarPesos(anyLong())).thenReturn(validacao);

        mockMvc.perform(get("/api/v1/criterios/validacao")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.total").value(100));
    }

    @Test
    @DisplayName("Deve retornar inválido quando soma diferente de 100")
    void testValidarCritériosSomaInvalida() throws Exception {
        ValidacaoCriteriosResponse validacao = new ValidacaoCriteriosResponse(false, 95);

        when(criterioService.validarPesos(anyLong())).thenReturn(validacao);

        mockMvc.perform(get("/api/v1/criterios/validacao")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.total").value(95));
    }
}
