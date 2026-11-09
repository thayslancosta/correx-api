package com.correx.service;

import com.correx.dto.TurmaRequest;
import com.correx.dto.TurmaResponse;
import com.correx.entity.Aluno;
import com.correx.entity.Professor;
import com.correx.enums.ProfessorRole;
import com.correx.entity.Turma;
import com.correx.exception.ResourceNotFoundException;
import com.correx.repository.AlunoRepository;
import com.correx.repository.ProfessorRepository;
import com.correx.repository.RedacaoRepository;
import com.correx.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TurmaService Unit Tests")
class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private RedacaoRepository redacaoRepository;

    @InjectMocks
    private TurmaService turmaService;

    private Professor professor;
    private Turma turma;
    private TurmaRequest turmaRequest;

    @BeforeEach
    void setUp() {
        professor = new Professor();
        professor.setId(1L);
        professor.setNome("Professor Demo");
        professor.setEmail("professor@correx.com");
        professor.setRole(ProfessorRole.PROFESSOR);
        professor.setAtivo(true);

        turma = new Turma();
        turma.setId(1L);
        turma.setNome("Inglês A2");
        turma.setAtiva(true);
        turma.setProfessor(professor);

        turmaRequest = new TurmaRequest("Inglês A2", true);
    }

    @Test
    @DisplayName("Deve listar todas as turmas do professor")
    void testListarTurmas() {
        Turma turma2 = new Turma();
        turma2.setId(2L);
        turma2.setNome("Inglês B1");
        turma2.setAtiva(true);
        turma2.setProfessor(professor);

        when(turmaRepository.findByProfessorId(1L))
                .thenReturn(Arrays.asList(turma, turma2));
        when(alunoRepository.findByTurmaId(1L))
                .thenReturn(Arrays.asList(new Aluno()));
        when(alunoRepository.findByTurmaId(2L))
                .thenReturn(Arrays.asList());

        var turmas = turmaService.listarTurmas(1L);

        assertNotNull(turmas);
        assertEquals(2, turmas.size());
        verify(turmaRepository, times(1)).findByProfessorId(1L);
    }

    @Test
    @DisplayName("Deve buscar turma por ID com sucesso")
    void testBuscarPorIdSuccess() {
        when(turmaRepository.findByIdAndProfessorId(1L, 1L))
                .thenReturn(Optional.of(turma));
        when(alunoRepository.findByTurmaId(1L))
                .thenReturn(Arrays.asList(new Aluno()));

        TurmaResponse response = turmaService.buscarPorId(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Inglês A2", response.nome());
        verify(turmaRepository, times(1)).findByIdAndProfessorId(1L, 1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar turma inexistente")
    void testBuscarPorIdNotFound() {
        when(turmaRepository.findByIdAndProfessorId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            turmaService.buscarPorId(999L, 1L);
        });

        verify(turmaRepository, times(1)).findByIdAndProfessorId(999L, 1L);
    }

    @Test
    @DisplayName("Deve criar turma com sucesso")
    void testCriarSuccess() {
        when(professorRepository.findById(1L))
                .thenReturn(Optional.of(professor));
        when(turmaRepository.save(any(Turma.class)))
                .thenReturn(turma);
        when(alunoRepository.findByTurmaId(anyLong()))
                .thenReturn(Arrays.asList());

        TurmaResponse response = turmaService.criar(turmaRequest, 1L);

        assertNotNull(response);
        assertEquals("Inglês A2", response.nome());
        assertTrue(response.ativa());
        verify(turmaRepository, times(1)).save(any(Turma.class));
    }

    @Test
    @DisplayName("Deve atualizar turma com sucesso")
    void testAtualizarSuccess() {
        TurmaRequest updateRequest = new TurmaRequest("Inglês A2 Avançado", false);
        Turma updatedTurma = new Turma();
        updatedTurma.setId(1L);
        updatedTurma.setNome("Inglês A2 Avançado");
        updatedTurma.setAtiva(false);
        updatedTurma.setProfessor(professor);

        when(turmaRepository.findByIdAndProfessorId(1L, 1L))
                .thenReturn(Optional.of(turma));
        when(turmaRepository.save(any(Turma.class)))
                .thenReturn(updatedTurma);
        when(alunoRepository.findByTurmaId(1L))
                .thenReturn(Arrays.asList());

        TurmaResponse response = turmaService.atualizar(1L, updateRequest, 1L);

        assertNotNull(response);
        assertEquals("Inglês A2 Avançado", response.nome());
        assertFalse(response.ativa());
        verify(turmaRepository, times(1)).save(any(Turma.class));
    }

    @Test
    @DisplayName("Deve deletar turma com sucesso")
    void testDeletarSuccess() {
        when(turmaRepository.findByIdAndProfessorId(1L, 1L))
                .thenReturn(Optional.of(turma));
        doNothing().when(turmaRepository).delete(turma);

        assertDoesNotThrow(() -> turmaService.deletar(1L, 1L));

        verify(turmaRepository, times(1)).delete(turma);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar turma inexistente")
    void testDeletarNotFound() {
        when(turmaRepository.findByIdAndProfessorId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            turmaService.deletar(999L, 1L);
        });

        verify(turmaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve listar opções de turmas ativas")
    void testListarOpcoes() {
        when(turmaRepository.findByProfessorIdAndAtivaTrue(1L))
                .thenReturn(Arrays.asList(turma));

        var opcoes = turmaService.listarOpcoes(1L);

        assertNotNull(opcoes);
        assertEquals(1, opcoes.size());
        verify(turmaRepository, times(1)).findByProfessorIdAndAtivaTrue(1L);
    }
}
