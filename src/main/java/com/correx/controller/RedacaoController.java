package com.correx.controller;

import com.correx.dto.*;
import com.correx.service.RedacaoService;
import com.correx.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/redacoes")
public class RedacaoController {

    private final RedacaoService redacaoService;
    private final SecurityUtils securityUtils;

    public RedacaoController(RedacaoService redacaoService, SecurityUtils securityUtils) {
        this.redacaoService = redacaoService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/texto")
    public ResponseEntity<RedacaoResponse> enviarTexto(@Valid @RequestBody RedacaoTextoRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(redacaoService.enviarTexto(request, professorId));
    }

    @PostMapping("/upload")
    public ResponseEntity<RedacaoResponse> enviarArquivo(
            @RequestParam MultipartFile arquivo,
            @RequestParam String idioma,
            @RequestParam(required = false) Long turmaId,
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) String nomeAluno) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(redacaoService.enviarArquivo(arquivo, idioma, turmaId, alunoId, nomeAluno, professorId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<RedacaoResumoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return ResponseEntity.ok(redacaoService.listarRedacoes(professorId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RedacaoResponse> buscarPorId(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(redacaoService.buscarPorId(id, professorId));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<PageResponse<RedacaoResumoResponse>> listarPorAluno(
            @PathVariable Long alunoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return ResponseEntity.ok(redacaoService.listarPorAluno(alunoId, professorId, pageable));
    }

    @PutMapping("/{id}/revisao")
    public ResponseEntity<RedacaoResponse> revisarCorrecao(@PathVariable Long id, @Valid @RequestBody RevisaoRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(redacaoService.revisarCorrecao(id, request, professorId));
    }

    @PutMapping("/{id}/recorrigir")
    public ResponseEntity<RedacaoResponse> recorrigir(@PathVariable Long id, @Valid @RequestBody RecorrecaoRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(redacaoService.recorrigirRedacao(id, request, professorId));
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<RedacaoResponse> enviarAoAluno(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(redacaoService.enviarAoAluno(id, professorId));
    }
}
