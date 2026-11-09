package com.correx.service;

import com.correx.dto.AlunoOptionResponse;
import com.correx.dto.AlunoRequest;
import com.correx.dto.AlunoResponse;
import com.correx.dto.PageResponse;
import com.correx.entity.Aluno;
import com.correx.entity.Turma;
import com.correx.exception.ResourceNotFoundException;
import com.correx.repository.AlunoRepository;
import com.correx.repository.TurmaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AlunoService(AlunoRepository alunoRepository, TurmaRepository turmaRepository) {
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
    }

    public PageResponse<AlunoResponse> listarAlunos(Long professorId, String search, Long turmaId, Pageable pageable) {
        Page<Aluno> page;
        
        if (search != null && !search.isBlank()) {
            page = alunoRepository.findByNomeContainingIgnoreCaseAndTurmaProfessorId(search, professorId, pageable);
        } else if (turmaId != null){
            Turma turma = turmaRepository.findByIdAndProfessorId(turmaId, professorId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
            page = alunoRepository.findByTurmaId(turmaId, pageable);
        } else {
            page = alunoRepository.findByTurmaProfessorId(professorId, pageable);
        }

        List<AlunoResponse> content = page.getContent().stream()
            .map(this::toAlunoResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public AlunoResponse buscarPorId(Long id, Long professorId) {
        Aluno aluno = alunoRepository.findByIdAndTurmaProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));
        return toAlunoResponse(aluno);
    }

    public AlunoResponse criar(AlunoRequest request, Long professorId) {
        Turma turma = turmaRepository.findByIdAndProfessorId(request.turmaId(), professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));

        Aluno aluno = Aluno.builder()
            .nome(request.nome())
            .email(request.email())
            .ativo(true)
            .turma(turma)
            .build();

        Aluno saved = alunoRepository.save(aluno);
        return toAlunoResponse(saved);
    }

    public AlunoResponse atualizar(Long id, AlunoRequest request, Long professorId) {
        Aluno aluno = alunoRepository.findByIdAndTurmaProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));

        aluno.setNome(request.nome());
        aluno.setEmail(request.email());

        Aluno updated = alunoRepository.save(aluno);
        return toAlunoResponse(updated);
    }

    public void deletar(Long id, Long professorId) {
        Aluno aluno = alunoRepository.findByIdAndTurmaProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));
        alunoRepository.delete(aluno);
    }

    private AlunoResponse toAlunoResponse(Aluno aluno) {
        int totalRedacoes = alunoRepository.countRedacoesByAlunoId(aluno.getId());
        Double media = alunoRepository.calcularMediaByAlunoId(aluno.getId());
        if (media == null) media = 0.0;

        LocalDateTime ultimaData = alunoRepository.ultimaAtividadeByAlunoId(aluno.getId());
        String ultimaAtividade = ultimaData != null
            ? ultimaData.format(DATE_FORMATTER)
            : "N/A";

        return new AlunoResponse(
            aluno.getId(),
            aluno.getNome(),
            aluno.getEmail(),
            aluno.getTurma().getNome(),
            media,
            totalRedacoes,
            ultimaAtividade
        );
    }
}
