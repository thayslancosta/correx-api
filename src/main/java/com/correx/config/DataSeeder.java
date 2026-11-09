package com.correx.config;

import com.correx.entity.*;
import com.correx.enums.OrigemRedacao;
import com.correx.enums.ProfessorRole;
import com.correx.enums.StatusRedacao;
import com.correx.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final CriterioAvaliacaoRepository criterioRepository;
    private final RedacaoRepository redacaoRepository;
    private final CorrecaoRepository correcaoRepository;
    private final NotaCriterioRepository notaCriterioRepository;
    private final ErroEncontradoRepository erroRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(ProfessorRepository professorRepository, TurmaRepository turmaRepository,
                     AlunoRepository alunoRepository, CriterioAvaliacaoRepository criterioRepository,
                     RedacaoRepository redacaoRepository, CorrecaoRepository correcaoRepository,
                     NotaCriterioRepository notaCriterioRepository, ErroEncontradoRepository erroRepository,
                     PasswordEncoder passwordEncoder) {
        this.professorRepository = professorRepository;
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.criterioRepository = criterioRepository;
        this.redacaoRepository = redacaoRepository;
        this.correcaoRepository = correcaoRepository;
        this.notaCriterioRepository = notaCriterioRepository;
        this.erroRepository = erroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (professorRepository.existsByEmail("professor@correx.com")) {
            log.info("Dados sementes já existem. Pulando init.");
            return;
        }

        log.info("Iniciando seeding de dados para dev...");

        // ─── 1. PROFESSOR ───────────────────────────────────────────────────────
        Professor professor = Professor.builder()
            .nome("Ana Paula Ferreira")
            .email("professor@correx.com")
            .senha(passwordEncoder.encode("123456"))
            .role(ProfessorRole.PROFESSOR)
            .ativo(true)
            .build();
        professorRepository.save(professor);
        log.info("Professor criado: {}", professor.getEmail());

        // ─── 2. TURMAS ──────────────────────────────────────────────────────────
        Turma turmaA2 = Turma.builder().nome("Inglês A2").ativa(true).professor(professor).build();
        Turma turmaB1 = Turma.builder().nome("Inglês B1").ativa(true).professor(professor).build();
        Turma turmaB2 = Turma.builder().nome("Inglês B2").ativa(true).professor(professor).build();
        Turma turmaEsp = Turma.builder().nome("Espanhol B1").ativa(true).professor(professor).build();
        Turma turmaInativa = Turma.builder().nome("Inglês A1 — Turma Encerrada").ativa(false).professor(professor).build();
        turmaRepository.save(turmaA2);
        turmaRepository.save(turmaB1);
        turmaRepository.save(turmaB2);
        turmaRepository.save(turmaEsp);
        turmaRepository.save(turmaInativa);
        log.info("5 turmas criadas");

        // ─── 3. ALUNOS ──────────────────────────────────────────────────────────
        Aluno maria    = Aluno.builder().nome("Maria Silva").email("maria.silva@email.com").ativo(true).turma(turmaB2).build();
        Aluno joao     = Aluno.builder().nome("João Santos").email("joao.santos@email.com").ativo(true).turma(turmaA2).build();
        Aluno ana      = Aluno.builder().nome("Ana Costa").email("ana.costa@email.com").ativo(true).turma(turmaEsp).build();
        Aluno pedro    = Aluno.builder().nome("Pedro Lima").email("pedro.lima@email.com").ativo(true).turma(turmaB1).build();
        Aluno lucas    = Aluno.builder().nome("Lucas Oliveira").email("lucas.oliveira@email.com").ativo(true).turma(turmaA2).build();
        Aluno beatriz  = Aluno.builder().nome("Beatriz Souza").email("beatriz.souza@email.com").ativo(true).turma(turmaB2).build();
        Aluno rafael   = Aluno.builder().nome("Rafael Mendes").email("rafael.mendes@email.com").ativo(true).turma(turmaB1).build();
        Aluno camila   = Aluno.builder().nome("Camila Rocha").email("camila.rocha@email.com").ativo(true).turma(turmaEsp).build();
        Aluno thiago   = Aluno.builder().nome("Thiago Alves").email("thiago.alves@email.com").ativo(true).turma(turmaB2).build();
        Aluno fernanda = Aluno.builder().nome("Fernanda Nunes").email("fernanda.nunes@email.com").ativo(true).turma(turmaB1).build();
        alunoRepository.save(maria);
        alunoRepository.save(joao);
        alunoRepository.save(ana);
        alunoRepository.save(pedro);
        alunoRepository.save(lucas);
        alunoRepository.save(beatriz);
        alunoRepository.save(rafael);
        alunoRepository.save(camila);
        alunoRepository.save(thiago);
        alunoRepository.save(fernanda);
        log.info("10 alunos criados");

        // ─── 4. CRITÉRIOS ───────────────────────────────────────────────────────
        CriterioAvaliacao c1  = CriterioAvaliacao.builder().nome("Neatness of Presentation").descricao("Clarity, organization, and visual presentation of the text").peso(5).ativo(true).professor(professor).build();
        CriterioAvaliacao c2  = CriterioAvaliacao.builder().nome("Task Comprehension").descricao("Understanding and fulfillment of the writing task requirements").peso(5).ativo(true).professor(professor).build();
        CriterioAvaliacao c3  = CriterioAvaliacao.builder().nome("Genre Adequacy").descricao("Appropriateness of the text to the required genre or format").peso(5).ativo(true).professor(professor).build();
        CriterioAvaliacao c4  = CriterioAvaliacao.builder().nome("Content").descricao("Quality, depth, and relevance of the ideas presented").peso(10).ativo(true).professor(professor).build();
        CriterioAvaliacao c5  = CriterioAvaliacao.builder().nome("Accuracy").descricao("Correctness of grammar, spelling, and punctuation").peso(20).ativo(true).professor(professor).build();
        CriterioAvaliacao c6  = CriterioAvaliacao.builder().nome("Vocabulary").descricao("Range, precision, and appropriate use of vocabulary").peso(15).ativo(true).professor(professor).build();
        CriterioAvaliacao c7  = CriterioAvaliacao.builder().nome("Cohesion").descricao("Logical flow, coherence, and use of linking devices").peso(10).ativo(true).professor(professor).build();
        CriterioAvaliacao c8  = CriterioAvaliacao.builder().nome("Creativity").descricao("Originality, creativity, and personal voice in expression").peso(10).ativo(true).professor(professor).build();
        CriterioAvaliacao c9  = CriterioAvaliacao.builder().nome("Use of the Language").descricao("Effective use of language structures, expressions, and idioms").peso(15).ativo(true).professor(professor).build();
        CriterioAvaliacao c10 = CriterioAvaliacao.builder().nome("Overall Impression").descricao("General quality, impact, and reader engagement").peso(5).ativo(true).professor(professor).build();
        criterioRepository.save(c1);
        criterioRepository.save(c2);
        criterioRepository.save(c3);
        criterioRepository.save(c4);
        criterioRepository.save(c5);
        criterioRepository.save(c6);
        criterioRepository.save(c7);
        criterioRepository.save(c8);
        criterioRepository.save(c9);
        criterioRepository.save(c10);
        log.info("10 critérios criados");

        // ─── 5. REDAÇÕES + CORREÇÕES ─────────────────────────────────────────────

        // ── Redação 1 — Maria (B2) — REVISADA — nota alta ───────────────────────
        Redacao r1 = Redacao.builder()
            .textoOriginal("Social media has fundamentally transformed the way people communicate and interact with one another. Platforms such as Instagram, Twitter, and TikTok have created new forms of expression and connection that were unimaginable two decades ago. However, this technological revolution comes with significant drawbacks that deserve careful consideration. The constant exposure to curated content can distort one's perception of reality, leading to anxiety and feelings of inadequacy. Moreover, the spread of misinformation poses a serious threat to democratic societies. It is therefore crucial that users develop digital literacy skills to navigate these platforms responsibly.")
            .textoEditado("Social media has fundamentally transformed the way people communicate and interact with one another. Platforms such as Instagram, Twitter, and TikTok have created new forms of expression and connection that were unimaginable two decades ago. However, this technological revolution comes with significant drawbacks that deserve careful consideration. The constant exposure to curated content can distort one's perception of reality, leading to anxiety and feelings of inadequacy. Moreover, the spread of misinformation poses a serious threat to democratic societies. It is therefore crucial that users develop digital literacy skills to navigate these platforms responsibly. [Revisado pela professora]")
            .idioma("en")
            .status(StatusRedacao.REVISADA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(maria)
            .professor(professor)
            .build();
        redacaoRepository.save(r1);

        Correcao cor1 = Correcao.builder()
            .notaFinal(9.2)
            .notaFinalManual(9.2)
            .comentarioGeral("Excelente redação! Maria demonstra domínio avançado da língua inglesa. O texto é bem estruturado, apresenta argumentos coesos e vocabulário sofisticado. Pequenos ajustes de estilo foram sugeridos na revisão.")
            .feedbackExtra("Parabéns pelo uso de conectivos variados e pela capacidade argumentativa. Continue assim!")
            .redacao(r1)
            .build();
        correcaoRepository.save(cor1);
        salvarNotas(cor1, new double[]{5.0, 5.0, 5.0, 9.5, 18.5, 14.0, 9.5, 9.0, 14.0, 5.0});
        erroRepository.save(ErroEncontrado.builder().categoria("Style").textoOriginal("comes with significant drawbacks").textoCorrigido("presents significant drawbacks").correcao(cor1).build());
        r1.setCorrecao(cor1);
        redacaoRepository.save(r1);

        // ── Redação 2 — João (A2) — CORRIGIDA — nota média ──────────────────────
        Redacao r2 = Redacao.builder()
            .textoOriginal("I think the internet is very good for people. We can find informations about everything in the internet. My favorite thing is watch videos on YouTube. Also, I can talk with my friends on WhatsApp every day. The internet help me a lot with my studies because I find many websites with exercises and videos. Sometimes internet is bad because people spend too much time. But in general I think is very useful for the modern life.")
            .idioma("en")
            .status(StatusRedacao.CORRIGIDA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(joao)
            .professor(professor)
            .build();
        redacaoRepository.save(r2);

        Correcao cor2 = Correcao.builder()
            .notaFinal(6.1)
            .comentarioGeral("João apresenta ideias relevantes, mas o texto tem vários erros gramaticais típicos do nível A2. É preciso trabalhar artigos, concordância e vocabulário. Boa iniciativa em abordar múltiplos aspectos do tema!")
            .feedbackExtra("Revise o uso de 'information' (incontável) e a estrutura 'I can + infinitivo sem 'to'. Continue praticando!")
            .redacao(r2)
            .build();
        correcaoRepository.save(cor2);
        salvarNotas(cor2, new double[]{3.5, 4.0, 3.5, 7.0, 10.0, 9.0, 6.0, 6.5, 9.5, 3.5});
        erroRepository.save(ErroEncontrado.builder().categoria("Grammar").textoOriginal("find informations about everything").textoCorrigido("find information about everything").correcao(cor2).build());
        erroRepository.save(ErroEncontrado.builder().categoria("Grammar").textoOriginal("I can talk with my friends").textoCorrigido("I can talk to my friends").correcao(cor2).build());
        erroRepository.save(ErroEncontrado.builder().categoria("Grammar").textoOriginal("The internet help me").textoCorrigido("The internet helps me").correcao(cor2).build());
        erroRepository.save(ErroEncontrado.builder().categoria("Grammar").textoOriginal("I think is very useful").textoCorrigido("I think it is very useful").correcao(cor2).build());
        erroRepository.save(ErroEncontrado.builder().categoria("Vocabulary").textoOriginal("watch videos on YouTube").textoCorrigido("watching videos on YouTube").correcao(cor2).build());
        r2.setCorrecao(cor2);
        redacaoRepository.save(r2);

        // ── Redação 3 — Ana (Espanhol) — CORRIGIDA — nota boa ───────────────────
        Redacao r3 = Redacao.builder()
            .textoOriginal("El cambio climático es uno de los problemas más graves que enfrenta la humanidad en el siglo XXI. Las actividades humanas, especialmente la quema de combustibles fósiles, han aumentado significativamente las concentraciones de gases de efecto invernadero en la atmósfera. Como consecuencia, las temperaturas globales están subiendo, los glaciares se derriten y los eventos climáticos extremos son cada vez más frecuentes. Para combatir este problema, es necesario que los gobiernos adopten políticas ambientales más estrictas y que los ciudadanos cambien sus hábitos de consumo.")
            .idioma("es")
            .status(StatusRedacao.CORRIGIDA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(ana)
            .professor(professor)
            .build();
        redacaoRepository.save(r3);

        Correcao cor3 = Correcao.builder()
            .notaFinal(8.3)
            .comentarioGeral("Muito boa redação em espanhol! Ana demonstra bom domínio da língua e apresenta argumentos bem fundamentados sobre as mudanças climáticas. O vocabulário é adequado e a estrutura está bem organizada.")
            .feedbackExtra("Excelente uso do subjuntivo na conclusão. Tente variar mais os conectores para enriquecer ainda mais o texto.")
            .redacao(r3)
            .build();
        correcaoRepository.save(cor3);
        salvarNotas(cor3, new double[]{4.5, 5.0, 4.5, 9.0, 17.0, 13.0, 8.5, 8.0, 13.0, 4.5});
        erroRepository.save(ErroEncontrado.builder().categoria("Vocabulary").textoOriginal("eventos climáticos extremos son cada vez más frecuentes").textoCorrigido("fenómenos climáticos extremos son cada vez más frecuentes").correcao(cor3).build());
        r3.setCorrecao(cor3);
        redacaoRepository.save(r3);

        // ── Redação 4 — Pedro (B1) — CORRIGIDA — nota média-alta ────────────────
        Redacao r4 = Redacao.builder()
            .textoOriginal("Working from home has become very popular since the pandemic. Many companies now allow their employees to work remotely, and this has both advantages and disadvantages. On one hand, people save time and money because they don't need to travel to the office. They can also have a better work-life balance. On the other hand, some people find it difficult to concentrate at home because of distractions. Communication with colleagues can also be more challenging. In my opinion, a hybrid model is the best solution because it combines the benefits of both options.")
            .idioma("en")
            .status(StatusRedacao.CORRIGIDA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(pedro)
            .professor(professor)
            .build();
        redacaoRepository.save(r4);

        Correcao cor4 = Correcao.builder()
            .notaFinal(7.8)
            .comentarioGeral("Pedro apresenta um texto bem equilibrado com argumentos dos dois lados. Boa estrutura e vocabulário adequado para o nível B1. Algumas estruturas poderiam ser mais elaboradas.")
            .feedbackExtra("Bom uso de 'on one hand / on the other hand'. Tente usar mais exemplos concretos para embasar seus argumentos.")
            .redacao(r4)
            .build();
        correcaoRepository.save(cor4);
        salvarNotas(cor4, new double[]{4.0, 5.0, 4.5, 8.0, 16.0, 12.0, 8.0, 7.5, 12.0, 4.5});
        erroRepository.save(ErroEncontrado.builder().categoria("Grammar").textoOriginal("they don't need to travel to the office").textoCorrigido("they don't have to travel to the office").correcao(cor4).build());
        r4.setCorrecao(cor4);
        redacaoRepository.save(r4);

        // ── Redação 5 — Lucas (A2) — PENDENTE ───────────────────────────────────
        Redacao r5 = Redacao.builder()
            .textoOriginal("My hobby is play video games. I like very much to play with my friends online. My favorite game is Minecraft because is very creative. In the weekend I play many hours. My parents say I play too much but I think is good for the brain because you need think a lot. Also I learn some English words with the games.")
            .idioma("en")
            .status(StatusRedacao.PENDENTE)
            .origem(OrigemRedacao.TEXTO)
            .aluno(lucas)
            .professor(professor)
            .build();
        redacaoRepository.save(r5);

        // ── Redação 6 — Beatriz (B2) — REVISADA — nota muito alta ───────────────
        Redacao r6 = Redacao.builder()
            .textoOriginal("Artificial intelligence is reshaping every aspect of modern life, from healthcare to entertainment, raising profound questions about the future of human work and creativity. While optimists argue that AI will free humanity from repetitive tasks, enabling us to focus on higher-order thinking and artistic endeavors, pessimists warn of unprecedented unemployment and the erosion of human agency. The truth, as always, lies somewhere in between. Rather than viewing AI as an existential threat or a panacea, societies must develop robust regulatory frameworks that harness its potential while mitigating its risks. Education systems must evolve to prioritize adaptability, critical thinking, and emotional intelligence — skills that machines cannot easily replicate.")
            .textoEditado("Artificial intelligence is reshaping every aspect of modern life, from healthcare to entertainment, raising profound questions about the future of human work and creativity. While optimists argue that AI will free humanity from repetitive tasks, enabling us to focus on higher-order thinking and artistic endeavors, pessimists warn of unprecedented unemployment and the erosion of human agency. The truth, as always, lies somewhere in between. Rather than viewing AI as an existential threat or a panacea, societies must develop robust regulatory frameworks that harness its potential while mitigating its risks. Education systems must evolve to prioritize adaptability, critical thinking, and emotional intelligence — skills that machines cannot easily replicate. [Revisado: excelente texto, nenhuma correção necessária]")
            .idioma("en")
            .status(StatusRedacao.REVISADA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(beatriz)
            .professor(professor)
            .build();
        redacaoRepository.save(r6);

        Correcao cor6 = Correcao.builder()
            .notaFinal(9.7)
            .notaFinalManual(9.7)
            .comentarioGeral("Texto excepcional! Beatriz demonstra domínio completo da língua inglesa com vocabulário acadêmico, estrutura argumentativa sólida e raciocínio crítico sofisticado. Este texto seria adequado para um exame de proficiência C1/C2.")
            .feedbackExtra("Impressionante uso de estruturas passivas, nominalizações e conectores sofisticados. Continue escrevendo com essa qualidade!")
            .redacao(r6)
            .build();
        correcaoRepository.save(cor6);
        salvarNotas(cor6, new double[]{5.0, 5.0, 5.0, 10.0, 19.5, 15.0, 10.0, 10.0, 15.0, 5.0});
        r6.setCorrecao(cor6);
        redacaoRepository.save(r6);

        // ── Redação 7 — Rafael (B1) — PENDENTE ──────────────────────────────────
        Redacao r7 = Redacao.builder()
            .textoOriginal("Travelling is one of the best ways to learn about different cultures and broaden your horizons. When you visit a new country, you experience different food, traditions, and ways of thinking. It can change your perspective on life. I have travelled to three countries so far: Argentina, Colombia, and Portugal. Each trip taught me something different. In Argentina, I learned about the passion for football and tango. In Colombia, I was impressed by the warmth of the people. Portugal made me realise how much I love the sea.")
            .idioma("en")
            .status(StatusRedacao.PENDENTE)
            .origem(OrigemRedacao.TEXTO)
            .aluno(rafael)
            .professor(professor)
            .build();
        redacaoRepository.save(r7);

        // ── Redação 8 — Camila (Espanhol) — CORRIGIDA ────────────────────────────
        Redacao r8 = Redacao.builder()
            .textoOriginal("La importancia de aprender idiomas en el mundo moderno es indiscutible. Con la globalización, la comunicación entre personas de diferentes países se ha vuelto esencial en los negocios, la ciencia y la cultura. Aprender un segundo idioma no solo abre puertas profesionales, sino que también mejora las funciones cognitivas del cerebro. Estudios demuestran que las personas bilingues tienen mejor memoria y mayor capacidad de concentración. Además, conocer otro idioma permite acceder a literaturas, películas y músicas en su idioma original, lo que enriquece enormemente la experiencia cultural.")
            .idioma("es")
            .status(StatusRedacao.CORRIGIDA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(camila)
            .professor(professor)
            .build();
        redacaoRepository.save(r8);

        Correcao cor8 = Correcao.builder()
            .notaFinal(8.7)
            .comentarioGeral("Excelente redação! Camila demonstra bom domínio do espanhol e apresenta argumentos bem fundamentados e atuais sobre a importância dos idiomas. A estrutura é clara e o vocabulário, diversificado.")
            .feedbackExtra("Atenção ao acento em 'bilingüe' — no espanhol moderno a forma correta é 'bilingüe' ou 'bilingue' dependendo do país. Ótimo texto!")
            .redacao(r8)
            .build();
        correcaoRepository.save(cor8);
        salvarNotas(cor8, new double[]{5.0, 5.0, 5.0, 9.0, 17.5, 13.5, 9.0, 8.5, 13.5, 5.0});
        erroRepository.save(ErroEncontrado.builder().categoria("Spelling").textoOriginal("personas bilingues").textoCorrigido("personas bilingües").correcao(cor8).build());
        r8.setCorrecao(cor8);
        redacaoRepository.save(r8);

        // ── Redação 9 — Thiago (B2) — CORRIGIDA ─────────────────────────────────
        Redacao r9 = Redacao.builder()
            .textoOriginal("The role of sports in society extends far beyond physical fitness and competition. Organized sports teach valuable life lessons such as teamwork, discipline, and resilience. For young people, participation in team sports can be particularly beneficial, as it provides a structured environment for developing social skills and building self-confidence. Furthermore, major sporting events like the Olympics and the FIFA World Cup have the power to unite nations and foster a sense of global community. Despite criticism regarding commercialization, sports remain one of the most powerful cultural forces in the modern world.")
            .idioma("en")
            .status(StatusRedacao.CORRIGIDA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(thiago)
            .professor(professor)
            .build();
        redacaoRepository.save(r9);

        Correcao cor9 = Correcao.builder()
            .notaFinal(8.9)
            .comentarioGeral("Ótima redação! Thiago demonstra boa fluência e capacidade argumentativa em inglês. O texto é coeso, bem estruturado e o vocabulário é variado e preciso. Apenas pequenas sugestões de estilo.")
            .feedbackExtra("Excelente uso de 'Furthermore' e 'Despite'. O parágrafo sobre os Jogos Olímpicos poderia ser mais desenvolvido com um exemplo específico.")
            .redacao(r9)
            .build();
        correcaoRepository.save(cor9);
        salvarNotas(cor9, new double[]{4.5, 5.0, 5.0, 9.0, 18.0, 14.0, 9.0, 9.0, 14.0, 4.5});
        erroRepository.save(ErroEncontrado.builder().categoria("Style").textoOriginal("the power to unite nations").textoCorrigido("the capacity to unite nations").correcao(cor9).build());
        r9.setCorrecao(cor9);
        redacaoRepository.save(r9);

        // ── Redação 10 — Fernanda (B1) — REVISADA ────────────────────────────────
        Redacao r10 = Redacao.builder()
            .textoOriginal("Healthy eating habits are essential for a long and happy life. Nowadays, many people eat too much processed food, which is high in sugar, salt, and unhealthy fats. This can lead to serious health problems such as obesity, diabetes, and heart disease. To maintain a balanced diet, it is important to eat a variety of fruits, vegetables, whole grains, and lean proteins. Drinking enough water is also very important. Small changes in our daily habits can make a big difference in our health in the long run.")
            .textoEditado("Healthy eating habits are essential for a long and happy life. Nowadays, many people eat too much processed food, which is high in sugar, salt, and unhealthy fats. This can lead to serious health problems such as obesity, diabetes, and heart disease. To maintain a balanced diet, it is important to eat a variety of fruits, vegetables, whole grains, and lean proteins. Drinking enough water is also crucial. Small changes in our daily habits can make a significant difference in our overall health in the long run. [Revisado: ótimo texto!]")
            .idioma("en")
            .status(StatusRedacao.REVISADA)
            .origem(OrigemRedacao.TEXTO)
            .aluno(fernanda)
            .professor(professor)
            .build();
        redacaoRepository.save(r10);

        Correcao cor10 = Correcao.builder()
            .notaFinal(7.5)
            .notaFinalManual(7.5)
            .comentarioGeral("Fernanda apresenta um texto claro e bem organizado sobre alimentação saudável. O vocabulário é adequado para o nível B1 e as ideias estão bem conectadas. Algumas substituições de vocabulário foram sugeridas na revisão.")
            .feedbackExtra("Boa progressão temática. Tente usar mais variedade de estruturas condicionais para enriquecer o texto.")
            .redacao(r10)
            .build();
        correcaoRepository.save(cor10);
        salvarNotas(cor10, new double[]{4.0, 4.5, 4.5, 8.0, 15.0, 11.0, 8.0, 7.5, 11.0, 4.5});
        erroRepository.save(ErroEncontrado.builder().categoria("Vocabulary").textoOriginal("very important").textoCorrigido("crucial").correcao(cor10).build());
        erroRepository.save(ErroEncontrado.builder().categoria("Vocabulary").textoOriginal("make a big difference").textoCorrigido("make a significant difference").correcao(cor10).build());
        r10.setCorrecao(cor10);
        redacaoRepository.save(r10);

        log.info("10 redações criadas (6 corrigidas, 2 revisadas, 2 pendentes)");
        log.info("═══════════════════════════════════════════════════");
        log.info("  CorreX — Seeding concluído com sucesso!");
        log.info("  Login: professor@correx.com / 123456");
        log.info("  Turmas: 5 | Alunos: 10 | Redações: 10");
        log.info("  Swagger: http://localhost:8080/swagger-ui/index.html");
        log.info("═══════════════════════════════════════════════════");
    }

    /**
     * Salva as 10 notas de critério para uma correção.
     * Ordem: Neatness, Task Comprehension, Genre, Content, Accuracy,
     *        Vocabulary, Cohesion, Creativity, Use of Language, Overall
     * Os pesos são: 5, 5, 5, 10, 20, 15, 10, 10, 15, 5
     */
    private void salvarNotas(Correcao correcao, double[] notas) {
        String[] nomes = {
            "Neatness of Presentation", "Task Comprehension", "Genre Adequacy",
            "Content", "Accuracy", "Vocabulary", "Cohesion",
            "Creativity", "Use of the Language", "Overall Impression"
        };
        int[] pesos = {5, 5, 5, 10, 20, 15, 10, 10, 15, 5};

        for (int i = 0; i < nomes.length; i++) {
            notaCriterioRepository.save(NotaCriterio.builder()
                .nomeCriterio(nomes[i])
                .peso(pesos[i])
                .nota(notas[i])
                .correcao(correcao)
                .build());
        }
    }
}
