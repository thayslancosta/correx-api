package com.correx.service;

import com.correx.dto.AlunoOptionResponse;
import com.correx.dto.TurmaOptionResponse;
import com.correx.dto.TurmaRequest;
import com.correx.dto.TurmaResponse;
import com.correx.entity.Professor;
import com.correx.entity.Turma;
import com.correx.exception.ResourceNotFoundException;
import com.correx.repository.AlunoRepository;
import com.correx.repository.ProfessorRepository;
import com.correx.repository.RedacaoRepository;
import com.correx.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final RedacaoRepository redacaoRepository;
    private final ProfessorRepository professorRepository;

    public TurmaService(TurmaRepository turmaRepository, AlunoRepository alunoRepository, 
                       RedacaoRepository redacaoRepository, ProfessorRepository professorRepository) {
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.redacaoRepository = redacaoRepository;
        this.professorRepository = professorRepository;
    }

    public List<TurmaResponse> listarTurmas(Long professorId) {
        return turmaRepository.findByProfessorId(professorId)
            .stream()
            .map(this::toTurmaResponse)
            .collect(Collectors.toList());
    }

    public TurmaResponse buscarPorId(Long id, Long professorId) {
        Turma turma = turmaRepository.findByIdAndProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
        return toTurmaResponse(turma);
    }

    public TurmaResponse criar(TurmaRequest request, Long professorId) {
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado"));

        Turma turma = Turma.builder()
            .nome(request.nome())
            .ativa(request.ativa() != null ? request.ativa() : true)
            .professor(professor)
            .build();

        Turma saved = turmaRepository.save(turma);
        return toTurmaResponse(saved);
    }

    public TurmaResponse atualizar(Long id, TurmaRequest request, Long professorId) {
        Turma turma = turmaRepository.findByIdAndProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));

        turma.setNome(request.nome());
        if (request.ativa() != null) {
            turma.setAtiva(request.ativa());
        }

        Turma updated = turmaRepository.save(turma);
        return toTurmaResponse(updated);
    }

    public void deletar(Long id, Long professorId) {
        Turma turma = turmaRepository.findByIdAndProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
        turmaRepository.delete(turma);
    }

    public List<TurmaOptionResponse> listarOpcoes(Long professorId) {
        return turmaRepository.findByProfessorIdAndAtivaTrue(professorId)
            .stream()
            .map(t -> new TurmaOptionResponse(t.getId(), t.getNome()))
            .collect(Collectors.toList());
    }

    public List<AlunoOptionResponse> listarAlunosOpcoes(Long turmaId, Long professorId) {
        Turma turma = turmaRepository.findByIdAndProfessorId(turmaId, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));

        return alunoRepository.findByTurmaId(turmaId)
            .stream()
            .filter(a -> a.getAtivo() != null && a.getAtivo())
            .map(a -> new AlunoOptionResponse(a.getId(), a.getNome()))
            .collect(Collectors.toList());
    }

    private TurmaResponse toTurmaResponse(Turma turma) {
        int totalAlunos = alunoRepository.findByTurmaId(turma.getId()).size();
        Double mediaGeral = calculateMediaGeral(turma.getId());
        return new TurmaResponse(turma.getId(), turma.getNome(), turma.getAtiva(), totalAlunos, mediaGeral);
    }

    private Double calculateMediaGeral(Long turmaId) {
        List<Long> alunoIds = alunoRepository.findByTurmaId(turmaId)
            .stream()
            .map(a -> a.getId())
            .collect(Collectors.toList());

        if (alunoIds.isEmpty()) return 0.0;

        double sum = 0.0;
        int count = 0;
        for (Long alunoId : alunoIds) {
            var redacoes = alunoRepository.findById(alunoId)
                .map(a -> a.getRedacoes())
                .orElseGet(List::of);
            
            for (var redacao : redacoes) {
                if (redacao.getCorrecao() != null && redacao.getCorrecao().getNotaFinal() != null) {
                    sum += redacao.getCorrecao().getNotaFinal();
                    count++;
                }
            }
        }

        return count > 0 ? sum / count : 0.0;
    }
}
