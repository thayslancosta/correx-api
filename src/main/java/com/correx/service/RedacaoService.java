package com.correx.service;

import com.correx.dto.*;
import com.correx.entity.*;
import com.correx.enums.OrigemRedacao;
import com.correx.enums.StatusRedacao;
import com.correx.exception.BusinessException;
import com.correx.exception.ResourceNotFoundException;
import com.correx.mapper.RedacaoMapper;
import com.correx.repository.*;
import com.correx.service.openai.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RedacaoService {

    private static final Logger logger = LoggerFactory.getLogger(RedacaoService.class);

    private final RedacaoRepository redacaoRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final CriterioAvaliacaoRepository criterioRepository;
    private final CorrecaoRepository correcaoRepository;
    private final NotaCriterioRepository notaCriterioRepository;
    private final ErroEncontradoRepository erroRepository;
    private final ProfessorRepository professorRepository;
    private final OpenAiService openAiService;
    private final FileService fileService;

    public RedacaoService(RedacaoRepository redacaoRepository, AlunoRepository alunoRepository, 
                        TurmaRepository turmaRepository, CriterioAvaliacaoRepository criterioRepository,
                        CorrecaoRepository correcaoRepository, NotaCriterioRepository notaCriterioRepository,
                        ErroEncontradoRepository erroRepository, ProfessorRepository professorRepository,
                        OpenAiService openAiService, FileService fileService) {
        this.redacaoRepository = redacaoRepository;
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.criterioRepository = criterioRepository;
        this.correcaoRepository = correcaoRepository;
        this.notaCriterioRepository = notaCriterioRepository;
        this.erroRepository = erroRepository;
        this.professorRepository = professorRepository;
        this.openAiService = openAiService;
        this.fileService = fileService;
    }

    public RedacaoResponse enviarTexto(RedacaoTextoRequest request, Long professorId) {
        validarCriterios(professorId);

        Aluno aluno = resolverAluno(request.alunoId(), request.turmaId(), request.nomeAluno(), professorId);
        Professor professor = obterProfessorGerenciado(professorId);

        Redacao redacao = Redacao.builder()
            .textoOriginal(request.texto().trim())
            .idioma(request.idioma())
            .status(StatusRedacao.PROCESSANDO)
            .origem(OrigemRedacao.TEXTO)
            .aluno(aluno)
            .professor(professor)
            .build();

        Redacao saved = redacaoRepository.save(redacao);

        try {
            corrigirRedacao(saved, professor, aluno, professorId);
            saved.setStatus(StatusRedacao.CORRIGIDA);
            redacaoRepository.save(saved);
            return RedacaoMapper.toResponse(saved);
        } catch (Exception ex) {
            saved.setStatus(StatusRedacao.ERRO);
            redacaoRepository.save(saved);
            throw ex;
        }
    }

    public RedacaoResponse enviarArquivo(MultipartFile arquivo, String idioma, Long turmaId, 
                                       Long alunoId, String nomeAluno, Long professorId) {
        validarCriterios(professorId);

        // Extrair texto do arquivo com suporte completo a PDFs
        String textoExtraido = extrairTextoDoArquivo(arquivo);
        var metadata = fileService.montarMetadados(arquivo);

        Aluno aluno = resolverAluno(alunoId, turmaId, nomeAluno, professorId);
        Professor professor = obterProfessorGerenciado(professorId);

        Redacao redacao = Redacao.builder()
            .textoOriginal(textoExtraido)
            .idioma(idioma)
            .status(StatusRedacao.PROCESSANDO)
            .origem(OrigemRedacao.ARQUIVO)
            .nomeArquivoOriginal(metadata.nomeOriginal())
            .tipoArquivo(metadata.tipoArquivo())
            .tamanhoArquivoBytes(metadata.tamanhoBytes())
            .aluno(aluno)
            .professor(professor)
            .build();

        Redacao saved = redacaoRepository.save(redacao);

        try {
            corrigirRedacao(saved, professor, aluno, professorId);
            saved.setStatus(StatusRedacao.CORRIGIDA);
            redacaoRepository.save(saved);
            return RedacaoMapper.toResponse(saved);
        } catch (Exception ex) {
            saved.setStatus(StatusRedacao.ERRO);
            redacaoRepository.save(saved);
            throw ex;
        }
    }

    /**
     * Extrai texto do arquivo, com tratamento especial para PDFs escaneados
     * 
     * Fluxo:
     * 1. Se for PDF, tenta extrair texto digital primeiro
     * 2. Se PDF for escaneado, faz OCR de todas as páginas
     * 3. Se for outro formato, extrai normalmente
     */
    private String extrairTextoDoArquivo(MultipartFile arquivo) {
        String filename = arquivo.getOriginalFilename();
        String extensao = obterExtensao(filename);

        // Para PDFs, usar extração completa com suporte a OCR
        if ("pdf".equalsIgnoreCase(extensao)) {
            return extrairTextoPdfComOcr(arquivo);
        }

        // Para outros formatos, usar extração simples
        return fileService.extrairTexto(arquivo);
    }

    /**
     * Extrai texto de PDF com suporte a OCR automático para PDFs escaneados
     */
    private String extrairTextoPdfComOcr(MultipartFile arquivo) {
        PdfExtractionResult resultado = fileService.extrairTextoPdfCompleto(arquivo);

        return switch (resultado.extractionType()) {
            case DIGITAL_TEXT -> {
                logger.info("PDF com texto digital. Páginas: {}, Caracteres: {}", 
                    resultado.totalPages(), resultado.getTextAsString().length());
                yield resultado.getTextAsString();
            }

            case SCANNED_OCR -> {
                logger.info("PDF escaneado detectado. Páginas: {}", resultado.totalPages());
                var base64Images = resultado.getImagesAsBase64List();
                
                logger.info("Iniciando OCR de {} página(s)...", base64Images.size());
                String textoExtraido = openAiService.extrairTextoDeImagensOcr(base64Images);
                if (textoExtraido == null || textoExtraido.isBlank()) {
                    throw new BusinessException("OCR do PDF não retornou texto utilizável");
                }
                
                logger.info("OCR completo. Total de caracteres: {}", textoExtraido.length());
                yield textoExtraido;
            }

            case OTHER_FORMAT -> {
                logger.warn("Tipo de extração não esperado para PDF: {}", resultado.extractionType());
                yield resultado.getTextAsString();
            }
        };
    }

    private String obterExtensao(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndex = filename.lastIndexOf(".");
        if (lastIndex > 0 && lastIndex < filename.length() - 1) {
            return filename.substring(lastIndex + 1);
        }
        return "";
    }

    private Professor obterProfessorGerenciado(Long professorId) {
        return professorRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado"));
    }

    public PageResponse<RedacaoResumoResponse> listarRedacoes(Long professorId, String status, Pageable pageable) {
        Page<Redacao> page;
        
        if (status != null && !status.isBlank()) {
            try {
                StatusRedacao statusEnum = StatusRedacao.valueOf(status.toUpperCase());
                page = redacaoRepository.findByProfessorIdAndStatus(professorId, statusEnum, pageable);
            } catch (IllegalArgumentException ex) {
                page = redacaoRepository.findByProfessorId(professorId, pageable);
            }
        } else {
            page = redacaoRepository.findByProfessorId(professorId, pageable);
        }

        List<RedacaoResumoResponse> content = page.getContent().stream()
            .map(RedacaoMapper::toResumoResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public PageResponse<RedacaoResumoResponse> listarPorAluno(Long alunoId, Long professorId, Pageable pageable) {
        Aluno aluno = alunoRepository.findByIdAndTurmaProfessorId(alunoId, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));

        Page<Redacao> page = redacaoRepository.findByAlunoIdAndProfessorId(alunoId, professorId, pageable);
        List<RedacaoResumoResponse> content = page.getContent().stream()
            .map(RedacaoMapper::toResumoResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public RedacaoResponse buscarPorId(Long id, Long professorId) {
        Redacao redacao = redacaoRepository.findByIdAndProfessorId(id, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Redação não encontrada"));
        return RedacaoMapper.toResponse(redacao);
    }

    public RedacaoResponse revisarCorrecao(Long redacaoId, RevisaoRequest request, Long professorId) {
        Redacao redacao = redacaoRepository.findByIdAndProfessorId(redacaoId, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Redação não encontrada"));

        if (redacao.getCorrecao() == null) {
            throw new BusinessException("Redação ainda não foi corrigida");
        }

        Correcao correcao = redacao.getCorrecao();
        if (request.textoEditado() != null) {
            redacao.setTextoEditado(request.textoEditado());
        }
        if (request.notaFinalManual() != null) {
            correcao.setNotaFinalManual(request.notaFinalManual());
        }
        if (request.feedbackExtra() != null) {
            correcao.setFeedbackExtra(request.feedbackExtra());
        }

        redacao.setStatus(StatusRedacao.REVISADA);
        redacaoRepository.save(redacao);
        correcaoRepository.save(correcao);

        return RedacaoMapper.toResponse(redacao);
    }

    public RedacaoResponse recorrigirRedacao(Long redacaoId, RecorrecaoRequest request, Long professorId) {
        validarCriterios(professorId);

        Redacao redacao = redacaoRepository.findByIdAndProfessorId(redacaoId, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Redação não encontrada"));

        if (request.textoEditado() == null || request.textoEditado().isBlank()) {
            throw new BusinessException("Texto editado não pode estar vazio");
        }

        redacao.setTextoEditado(request.textoEditado().trim());
        redacao.setStatus(StatusRedacao.PROCESSANDO);
        redacaoRepository.save(redacao);

        try {
            recorrigirEstruturaDaRedacao(redacao, professorId);
            redacao.setStatus(StatusRedacao.CORRIGIDA);
            redacaoRepository.save(redacao);
            return RedacaoMapper.toResponse(redacao);
        } catch (Exception ex) {
            redacao.setStatus(StatusRedacao.ERRO);
            redacaoRepository.save(redacao);
            throw ex;
        }
    }

    public RedacaoResponse enviarAoAluno(Long redacaoId, Long professorId) {
        Redacao redacao = redacaoRepository.findByIdAndProfessorId(redacaoId, professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Redação não encontrada"));

        redacao.setStatus(StatusRedacao.ENVIADA);
        redacaoRepository.save(redacao);
        return RedacaoMapper.toResponse(redacao);
    }

    private void validarCriterios(Long professorId) {
        List<CriterioAvaliacao> criterios = criterioRepository.findByProfessorIdAndAtivoTrue(professorId);
        int totalPesos = criterios.stream().mapToInt(CriterioAvaliacao::getPeso).sum();
        if (totalPesos != 100) {
            throw new BusinessException("Os critérios de avaliação devem somar 100% antes de enviar para correção.");
        }
    }

    private Aluno resolverAluno(Long alunoId, Long turmaId, String nomeAluno, Long professorId) {
        if (alunoId != null) {
            return alunoRepository.findByIdAndTurmaProfessorId(alunoId, professorId)
                .orElseThrow(() -> new BusinessException("Aluno não encontrado"));
        }

        if (turmaId == null) {
            throw new BusinessException("Turma é obrigatória quando alunoId não é fornecido");
        }

        if (nomeAluno == null || nomeAluno.isBlank()) {
            throw new BusinessException("Nome do aluno é obrigatório quando alunoId não é fornecido");
        }

        Turma turma = turmaRepository.findByIdAndProfessorId(turmaId, professorId)
            .orElseThrow(() -> new BusinessException("Turma não encontrada"));

        var alunosExistentes = alunoRepository.findByNomeContainingIgnoreCaseAndTurmaProfessorId(nomeAluno.trim(), professorId);
        if (!alunosExistentes.isEmpty()) {
            return alunosExistentes.get(0);
        }

        Aluno novoAluno = Aluno.builder()
            .nome(nomeAluno.trim())
            .ativo(true)
            .turma(turma)
            .build();

        return alunoRepository.save(novoAluno);
    }

    private void corrigirRedacao(Redacao redacao, Professor professor, Aluno aluno, Long professorId) {
        List<CriterioAvaliacao> criterios = criterioRepository.findByProfessorIdAndAtivoTrue(professorId);
        
        var resultado = openAiService.corrigirRedacao(redacao.getTextoOriginal(), redacao.getIdioma(), criterios);

        Correcao correcao = Correcao.builder()
            .notaFinal(resultado.getNotaFinal())
            .comentarioGeral(resultado.getComentarioGeral())
            .redacao(redacao)
            .build();

        Correcao correcaoSalva = correcaoRepository.save(correcao);

        for (var nota : resultado.getNotas()) {
            NotaCriterio notaCriterio = NotaCriterio.builder()
                .nomeCriterio(nota.getNomeCriterio())
                .peso(nota.getPeso())
                .nota(nota.getNota())
                .correcao(correcaoSalva)
                .build();
            notaCriterioRepository.save(notaCriterio);
        }

        for (var erro : resultado.getErros()) {
            ErroEncontrado erroEncontrado = ErroEncontrado.builder()
                .categoria(erro.getCategoria())
                .textoOriginal(erro.getTextoOriginal())
                .textoCorrigido(erro.getTextoCorrigido())
                .correcao(correcaoSalva)
                .build();
            erroRepository.save(erroEncontrado);
        }

        redacao.setCorrecao(correcaoSalva);
    }

    private void recorrigirEstruturaDaRedacao(Redacao redacao, Long professorId) {
        List<CriterioAvaliacao> criterios = criterioRepository.findByProfessorIdAndAtivoTrue(professorId);
        String textoBase = redacao.getTextoEditado() != null && !redacao.getTextoEditado().isBlank()
            ? redacao.getTextoEditado()
            : redacao.getTextoOriginal();

        var resultado = openAiService.corrigirRedacao(textoBase, redacao.getIdioma(), criterios);

        Correcao correcao = correcaoRepository.findByRedacaoId(redacao.getId())
            .orElseGet(() -> Correcao.builder().redacao(redacao).build());

        Long correcaoIdExistente = correcao.getId();
        String feedbackExtraAtual = correcao.getFeedbackExtra();
        Double notaFinalManualAtual = correcao.getNotaFinalManual();

        correcao.setNotaFinal(resultado.getNotaFinal());
        correcao.setComentarioGeral(resultado.getComentarioGeral());
        correcao.setFeedbackExtra(feedbackExtraAtual);
        correcao.setNotaFinalManual(notaFinalManualAtual);

        Correcao correcaoSalva = correcaoRepository.save(correcao);

        if (correcaoIdExistente != null) {
            notaCriterioRepository.deleteByCorrecaoId(correcaoIdExistente);
            erroRepository.deleteByCorrecaoId(correcaoIdExistente);
        }

        for (var nota : resultado.getNotas()) {
            notaCriterioRepository.save(NotaCriterio.builder()
                .nomeCriterio(nota.getNomeCriterio())
                .peso(nota.getPeso())
                .nota(nota.getNota())
                .correcao(correcaoSalva)
                .build());
        }

        for (var erro : resultado.getErros()) {
            erroRepository.save(ErroEncontrado.builder()
                .categoria(erro.getCategoria())
                .textoOriginal(erro.getTextoOriginal())
                .textoCorrigido(erro.getTextoCorrigido())
                .correcao(correcaoSalva)
                .build());
        }

        redacao.setCorrecao(correcaoSalva);
    }
}
