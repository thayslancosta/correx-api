package com.correx.controller;

import com.correx.dto.CriterioRequest;
import com.correx.dto.CriterioResponse;
import com.correx.dto.ValidacaoCriteriosResponse;
import com.correx.service.CriterioService;
import com.correx.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/criterios")
public class CriterioController {

    private final CriterioService criterioService;
    private final SecurityUtils securityUtils;

    public CriterioController(CriterioService criterioService, SecurityUtils securityUtils) {
        this.criterioService = criterioService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<CriterioResponse>> listar() {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(criterioService.listarCriterios(professorId));
    }

    @PostMapping
    public ResponseEntity<CriterioResponse> criar(@Valid @RequestBody CriterioRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(criterioService.criar(request, professorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CriterioResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CriterioRequest request) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(criterioService.atualizar(id, request, professorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        criterioService.deletar(id, professorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validacao")
    public ResponseEntity<ValidacaoCriteriosResponse> validar() {
        Long professorId = securityUtils.getProfessorAutenticado().getId();
        return ResponseEntity.ok(criterioService.validarPesos(professorId));
    }
}
