package com.correx.controller;

import com.correx.dto.AlunoRequest;
import com.correx.dto.AlunoResponse;
import com.correx.dto.PageResponse;
import com.correx.entity.Professor;
import com.correx.security.JwtAuthenticationFilter;
import com.correx.security.RateLimitFilter;
import com.correx.service.AlunoService;
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

@WebMvcTest(AlunoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AlunoController Tests")
@WithMockUser(username = "professor@correx.com", roles = "PROFESSOR")
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlunoService alunoService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private AlunoResponse alunoResponse;

    @BeforeEach
    void setUp() {
        Professor professor = Professor.builder()
                .id(1L)
                .email("professor@correx.com")
                .nome("Professor")
                .build();
        lenient().when(securityUtils.getProfessorAutenticado()).thenReturn(professor);

        alunoResponse = new AlunoResponse(1L, "Maria Silva", "maria@example.com", "Inglês A2", 7.5, 3, "11/04/2026");
    }

    @Test
    @DisplayName("Deve listar alunos com paginação")
    void testListarAlunos() throws Exception {
        PageResponse<AlunoResponse> pageResponse = new PageResponse<>(
                Arrays.asList(alunoResponse),
                0,
                10,
                1,
                1
        );

        when(alunoService.listarAlunos(anyLong(), nullable(String.class), nullable(Long.class), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/alunos?page=0&size=10")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Deve buscar aluno por ID")
    void testBuscarAlunoPorId() throws Exception {
        when(alunoService.buscarPorId(1L, 1L)).thenReturn(alunoResponse);

        mockMvc.perform(get("/api/v1/alunos/1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria@example.com"));
    }

    @Test
    @DisplayName("Deve criar aluno com sucesso")
    void testCriarAluno() throws Exception {
        AlunoRequest request = new AlunoRequest("João Santos", "joao@example.com", 1L);
        AlunoResponse response = new AlunoResponse(2L, "João Santos", "joao@example.com", "Inglês B1", 0.0, 0, "");

        when(alunoService.criar(any(AlunoRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(post("/api/v1/alunos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("João Santos"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar aluno sem nome")
    void testCriarAlunoSemNome() throws Exception {
        AlunoRequest request = new AlunoRequest("", "aluno@example.com", 1L);

        mockMvc.perform(post("/api/v1/alunos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar aluno com email inválido")
    void testCriarAlunoComEmailInvalido() throws Exception {
        AlunoRequest request = new AlunoRequest("Test Aluno", "email-invalido", 1L);

        mockMvc.perform(post("/api/v1/alunos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve atualizar aluno com sucesso")
    void testAtualizarAluno() throws Exception {
        AlunoRequest request = new AlunoRequest("Maria Silva Updated", "maria.updated@example.com", 1L);
        AlunoResponse response = new AlunoResponse(1L, "Maria Silva Updated", "maria.updated@example.com", "Inglês A2", 7.5, 3, "11/04/2026");

        when(alunoService.atualizar(eq(1L), any(AlunoRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(put("/api/v1/alunos/1")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva Updated"));
    }

    @Test
    @DisplayName("Deve deletar aluno com sucesso")
    void testDeletarAluno() throws Exception {
        doNothing().when(alunoService).deletar(eq(1L), anyLong());

        mockMvc.perform(delete("/api/v1/alunos/1")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
