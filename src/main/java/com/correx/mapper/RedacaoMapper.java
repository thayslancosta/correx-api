package com.correx.mapper;

import com.correx.dto.CorrecaoResponse;
import com.correx.dto.ErroResponse;
import com.correx.dto.NotaCriterioResponse;
import com.correx.dto.RedacaoResumoResponse;
import com.correx.dto.RedacaoResponse;
import com.correx.entity.Correcao;
import com.correx.entity.Redacao;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RedacaoMapper {

    public static RedacaoResponse toResponse(Redacao r) {
        CorrecaoResponse correcaoResponse = null;

        // Acessa correcao SOMENTE se já estiver carregada (evita lazy load)
        try {
            Correcao c = r.getCorrecao();
            if (c != null) {
                List<NotaCriterioResponse> notas = Collections.emptyList();
                List<ErroResponse> erros = Collections.emptyList();

                try {
                    if (c.getNotas() != null) {
                        notas = c.getNotas().stream()
                            .map(n -> new NotaCriterioResponse(n.getNomeCriterio(), n.getPeso(), n.getNota()))
                            .collect(Collectors.toList());
                    }
                } catch (Exception ignored) {}

                try {
                    if (c.getErros() != null) {
                        erros = c.getErros().stream()
                            .map(e -> new ErroResponse(e.getCategoria(), e.getTextoOriginal(), e.getTextoCorrigido()))
                            .collect(Collectors.toList());
                    }
                } catch (Exception ignored) {}

                correcaoResponse = new CorrecaoResponse(
                    c.getId(),
                    c.getNotaFinal(),
                    c.getNotaFinalManual(),
                    c.getComentarioGeral(),
                    c.getFeedbackExtra(),
                    notas,
                    erros
                );
            }
        } catch (Exception ignored) {}

        String nomeTurma = null;
        try {
            nomeTurma = r.getAluno().getTurma().getNome();
        } catch (Exception ignored) {}

        String nomeAluno = null;
        try {
            nomeAluno = r.getAluno().getNome();
        } catch (Exception ignored) {}

        return new RedacaoResponse(
            r.getId(),
            r.getTextoOriginal(),
            r.getTextoEditado(),
            r.getIdioma(),
            r.getStatus() != null ? r.getStatus().name() : null,
            r.getOrigem() != null ? r.getOrigem().name() : null,
            r.getNomeArquivoOriginal(),
            r.getTipoArquivo(),
            r.getTamanhoArquivoBytes(),
            r.getAluno() != null ? r.getAluno().getId() : null,
            nomeAluno,
            nomeTurma,
            correcaoResponse,
            r.getCreatedAt()
        );
    }

    public static RedacaoResumoResponse toResumoResponse(Redacao r) {
        Double nota = null;
        try {
            if (r.getCorrecao() != null) {
                nota = r.getCorrecao().getNotaFinal();
            }
        } catch (Exception ignored) {}

        String nomeAluno = null;
        try {
            nomeAluno = r.getAluno().getNome();
        } catch (Exception ignored) {}

        String nomeTurma = null;
        try {
            nomeTurma = r.getAluno().getTurma().getNome();
        } catch (Exception ignored) {}

        return new RedacaoResumoResponse(
            r.getId(),
            nomeAluno,
            nomeTurma,
            nota,
            r.getStatus() != null ? r.getStatus().name() : null,
            r.getCreatedAt()
        );
    }
}
