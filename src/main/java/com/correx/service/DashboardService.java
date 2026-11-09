package com.correx.service;

import com.correx.dto.DashboardCorrecaoResumoResponse;
import com.correx.dto.DashboardResponse;
import com.correx.entity.Redacao;
import com.correx.enums.StatusRedacao;
import com.correx.mapper.DashboardMapper;
import com.correx.repository.RedacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardService {

    private final RedacaoRepository redacaoRepository;

    public DashboardService(RedacaoRepository redacaoRepository) {
        this.redacaoRepository = redacaoRepository;
    }

    public DashboardResponse getDashboard(Long professorId) {
        long totalCorrecoes = redacaoRepository.countByProfessorIdAndStatus(professorId, StatusRedacao.CORRIGIDA) +
            redacaoRepository.countByProfessorIdAndStatus(professorId, StatusRedacao.REVISADA) +
            redacaoRepository.countByProfessorIdAndStatus(professorId, StatusRedacao.ENVIADA);

        long alunosAtivos = calcularAlunosAtivos(professorId);
        Double mediaGeral = calcularMediaGeral(professorId);
        
        long pendentes = redacaoRepository.countByProfessorIdAndStatus(professorId, StatusRedacao.PENDENTE) +
            redacaoRepository.countByProfessorIdAndStatus(professorId, StatusRedacao.PROCESSANDO);

        List<DashboardCorrecaoResumoResponse> correcoesRecentes = redacaoRepository.findTop5ByProfessorIdOrderByCreatedAtDesc(professorId)
            .stream()
            .map(DashboardMapper::toResumoResponse)
            .collect(Collectors.toList());

        return new DashboardResponse(totalCorrecoes, alunosAtivos, mediaGeral, pendentes, correcoesRecentes);
    }

    private long calcularAlunosAtivos(Long professorId) {
        Set<Long> alunoIds = new HashSet<>();
        List<Redacao> redacoes = redacaoRepository.findByProfessorId(professorId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();
        redacoes.forEach(r -> alunoIds.add(r.getAluno().getId()));
        return alunoIds.size();
    }

    private Double calcularMediaGeral(Long professorId) {
        List<Redacao> redacoes = redacaoRepository.findByProfessorId(professorId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
            .getContent();

        double soma = 0;
        int count = 0;

        for (Redacao redacao : redacoes) {
            if (redacao.getCorrecao() != null && redacao.getCorrecao().getNotaFinal() != null) {
                soma += redacao.getCorrecao().getNotaFinal();
                count++;
            }
        }

        return count > 0 ? Math.round(soma / count * 10.0) / 10.0 : 0.0;
    }
}
