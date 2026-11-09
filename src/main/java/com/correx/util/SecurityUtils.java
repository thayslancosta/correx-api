package com.correx.util;

import com.correx.entity.Professor;
import com.correx.exception.UnauthorizedException;
import com.correx.repository.ProfessorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final ProfessorRepository professorRepository;

    public SecurityUtils(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public static String getEmailAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new UnauthorizedException("Usuário não autenticado");
    }

    public Professor getProfessorAutenticado() {
        String email = getEmailAutenticado();
        return professorRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Professor não encontrado"));
    }
}
