package com.correx.service;

import com.correx.dto.CriterioRequest;
import com.correx.dto.CriterioResponse;
import com.correx.dto.ValidacaoCriteriosResponse;
import com.correx.entity.CriterioAvaliacao;
import com.correx.entity.Professor;
import com.correx.exception.ResourceNotFoundException;
import com.correx.repository.CriterioAvaliacaoRepository;
import com.correx.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CriterioService {

    private final CriterioAvaliacaoRepository criterioRepository;
    private final ProfessorRepository professorRepository;

    public CriterioService(CriterioAvaliacaoRepository criterioRepository, ProfessorRepository professorRepository) {
        this.criterioRepository = criterioRepository;
        this.professorRepository = professorRepository;
    }

    public List<CriterioResponse> listarCriterios(Long professorId) {
        return criterioRepository.findByProfessorId(professorId).stream()
            .map(this::toCriterioResponse)
            .collect(Collectors.toList());
    }

    public CriterioResponse criar(CriterioRequest request, Long professorId) {
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado"));

        CriterioAvaliacao criterio = CriterioAvaliacao.builder()
            .nome(request.nome())
            .descricao(request.descricao())
            .peso(request.peso())
            .ativo(request.ativo() != null ? request.ativo() : true)
            .professor(professor)
            .build();

        CriterioAvaliacao saved = criterioRepository.save(criterio);
        return toCriterioResponse(saved);
    }

    public CriterioResponse atualizar(Long id, CriterioRequest request, Long professorId) {
        CriterioAvaliacao criterio = criterioRepository.findByIdAndProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Critério não encontrado"));

        criterio.setNome(request.nome());
        criterio.setDescricao(request.descricao());
        criterio.setPeso(request.peso());
        if (request.ativo() != null) {
            criterio.setAtivo(request.ativo());
        }

        CriterioAvaliacao updated = criterioRepository.save(criterio);
        return toCriterioResponse(updated);
    }

    public void deletar(Long id, Long professorId) {
        CriterioAvaliacao criterio = criterioRepository.findByIdAndProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Critério não encontrado"));
        criterioRepository.delete(criterio);
    }

    public ValidacaoCriteriosResponse validarPesos(Long professorId) {
        List<CriterioAvaliacao> criterios = criterioRepository.findByProfessorIdAndAtivoTrue(professorId);
        int totalPesos = criterios.stream().mapToInt(CriterioAvaliacao::getPeso).sum();
        boolean valido = totalPesos == 100;
        return new ValidacaoCriteriosResponse(valido, totalPesos);
    }

    private CriterioResponse toCriterioResponse(CriterioAvaliacao criterio) {
        return new CriterioResponse(criterio.getId(), criterio.getNome(), criterio.getDescricao(),
            criterio.getPeso(), criterio.getAtivo());
    }
}
