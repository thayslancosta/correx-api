package com.correx.controller;

import com.correx.dto.AlunoRequest;
import com.correx.dto.AlunoResponse;
import com.correx.dto.PageResponse;
import com.correx.service.AlunoService;
import com.correx.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final SecurityUtils securityUtils;

    public AlunoController(AlunoService alunoService, SecurityUtils securityUtils) {
        this.alunoService = alunoService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AlunoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long turmaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(alunoService.listarAlunos(professorId, search, turmaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoResponse> buscarPorId(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(alunoService.buscarPorId(id, professorId));
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> criar(@Valid @RequestBody AlunoRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(alunoService.criar(request, professorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlunoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(alunoService.atualizar(id, request, professorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        alunoService.deletar(id, professorId);
        return ResponseEntity.noContent().build();
    }
}
