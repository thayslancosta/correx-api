package com.correx.controller;

import com.correx.dto.AlunoOptionResponse;
import com.correx.dto.TurmaOptionResponse;
import com.correx.dto.TurmaRequest;
import com.correx.dto.TurmaResponse;
import com.correx.service.TurmaService;
import com.correx.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/turmas")
public class TurmaController {

    private final TurmaService turmaService;
    private final SecurityUtils securityUtils;

    public TurmaController(TurmaService turmaService, SecurityUtils securityUtils) {
        this.turmaService = turmaService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<TurmaResponse>> listar() {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(turmaService.listarTurmas(professorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurmaResponse> buscarPorId(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(turmaService.buscarPorId(id, professorId));
    }

    @PostMapping
    public ResponseEntity<TurmaResponse> criar(@Valid @RequestBody TurmaRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.criar(request, professorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurmaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody TurmaRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(turmaService.atualizar(id, request, professorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        turmaService.deletar(id, professorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/opcoes")
    public ResponseEntity<List<TurmaOptionResponse>> listarOpcoes() {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(turmaService.listarOpcoes(professorId));
    }

    @GetMapping("/{id}/alunos/opcoes")
    public ResponseEntity<List<AlunoOptionResponse>> listarAlunosOpcoes(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(turmaService.listarAlunosOpcoes(id, professorId));
    }
}
