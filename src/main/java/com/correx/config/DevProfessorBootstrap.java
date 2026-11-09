package com.correx.config;

import com.correx.entity.Professor;
import com.correx.entity.CriterioAvaliacao;
import com.correx.enums.ProfessorRole;
import com.correx.repository.CriterioAvaliacaoRepository;
import com.correx.repository.ProfessorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.dev-user.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class DevProfessorBootstrap implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final CriterioAvaliacaoRepository criterioRepository;
    private final PasswordEncoder passwordEncoder;

    public DevProfessorBootstrap(
        ProfessorRepository professorRepository,
        CriterioAvaliacaoRepository criterioRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.professorRepository = professorRepository;
        this.criterioRepository = criterioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = "professor@correx.com";

        Professor professor = professorRepository.findByEmail(email).orElse(null);
        if (professor == null) {
            professor = Professor.builder()
                .nome("Ana Paula Ferreira")
                .email(email)
                .senha(passwordEncoder.encode("123456"))
                .role(ProfessorRole.PROFESSOR)
                .ativo(true)
                .build();

            professor = professorRepository.save(professor);
            log.info("Usuário padrão de desenvolvimento criado: {}", email);
        }

        garantirCriteriosPadrao(professor);
    }

    private void garantirCriteriosPadrao(Professor professor) {
        if (!criterioRepository.findByProfessorId(professor.getId()).isEmpty()) {
            return;
        }

        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Neatness of Presentation")
            .descricao("Clarity, organization, and visual presentation of the text")
            .peso(5)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Task Comprehension")
            .descricao("Understanding and fulfillment of the writing task requirements")
            .peso(5)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Genre Adequacy")
            .descricao("Appropriateness of the text to the required genre or format")
            .peso(5)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Content")
            .descricao("Quality, depth, and relevance of the ideas presented")
            .peso(10)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Accuracy")
            .descricao("Correctness of grammar, spelling, and punctuation")
            .peso(20)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Vocabulary")
            .descricao("Range, precision, and appropriate use of vocabulary")
            .peso(15)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Cohesion")
            .descricao("Logical flow, coherence, and use of linking devices")
            .peso(10)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Creativity")
            .descricao("Originality, creativity, and personal voice in expression")
            .peso(10)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Use of the Language")
            .descricao("Effective use of language structures, expressions, and idioms")
            .peso(15)
            .ativo(true)
            .professor(professor)
            .build());
        criterioRepository.save(CriterioAvaliacao.builder()
            .nome("Overall Impression")
            .descricao("General quality, impact, and reader engagement")
            .peso(5)
            .ativo(true)
            .professor(professor)
            .build());

        log.info("Critérios padrão de desenvolvimento criados para {}", professor.getEmail());
    }
}
