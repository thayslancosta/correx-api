# CorreX — Diagnóstico pré-deploy e plano de trabalho da equipe

**Diagnóstico feito em:** 25/11/2026 · **Última atualização:** 25/11/2026 (Fase 0 concluída)
**Responsável:** Thayslan Costa, dono dos repositórios e revisor dos PRs
**Objetivo:** colocar o CorreX em um **piloto real com 2 professores**, na hospedagem mais barata possível, sem bugs que percam dados e sem expor segredos.
**Situação atual:** Fase 0 concluída. **A equipe começa pela Fase 1.**

> Este arquivo é lido por pessoas **e** por assistentes de IA (Codex, ChatGPT, Claude, GitHub Copilot).
> **Se você é uma IA:** leia as Partes A, B e C inteiras antes de qualquer alteração. Depois, leia na Parte D só a tarefa que foi atribuída a você.

---

## Como este documento está organizado

| Parte | Conteúdo | Quem precisa ler |
|---|---|---|
| **A. Regras** | Fluxo Git e regras que ninguém pode quebrar | Todos, antes de começar |
| **B. Contexto** | O que é o CorreX, stack, endpoints, como rodar | Todos, antes de começar |
| **C. Diagnóstico** | O que foi testado e o que está quebrado | Todos, antes de começar |
| **D. Plano** | **As fases (0 a 4)** e as tarefas de cada uma | Cada pessoa, na sua tarefa |
| **E. Pronto para o piloto** | Checklist final de liberação | Thayslan e QA |
| **F. Depois do piloto** | Melhorias sem prazo e limites deste diagnóstico | Consulta |

**Como funcionam as fases:** são **5 fases em sequência** (0 a 4). A equipe inteira trabalha na mesma fase ao mesmo tempo, e dentro dela as tarefas rodam em paralelo, respeitando o campo "Depende de". **A próxima fase só começa quando todas as tarefas da fase atual estiverem concluídas.** Cada tarefa tem um número (`2.1` é a tarefa 1 da Fase 2) e uma etiqueta de área: `[Backend]`, `[Frontend]`, `[Infra]`, `[QA]` ou `[Docs]`.

---

# Parte A — Regras

### Fluxo Git

- **Uma branch por tarefa**, criada a partir da `main` atualizada. Nome: `<tipo>/<número>-<resumo>`.
  Exemplos: `ci/1.1-github-actions`, `fix/2.1-redacao-perdida`, `feat/2.5-rota-protegida`.
  Tipos: `feat`, `fix`, `test`, `ci`, `chore`, `docs`, `refactor`.
- **Um PR por tarefa**, pequeno. Título: `[número] descrição curta` (ex.: `[2.1] Persistir status ERRO quando a IA falhar`).
- A descrição do PR traz: o que mudou, como foi testado (comandos e resultado) e o critério de aceite marcado.
- A `main` é protegida: exige PR com 1 aprovação, sem push direto. **O Thayslan revisa e aprova.** PRs que mexem em segurança, configuração ou migrações (`application.yml`, `SecurityConfig`, `db/migration`, `.github/`, `Dockerfile`) **sempre** passam por ele.
- Commits em português, no imperativo: `Corrige rollback ao falhar a correção por IA`.
- Antes de abrir o PR, rode as verificações da sua área (Parte B, "Como rodar"). Depois da tarefa 1.1, o PR só entra com a CI verde.
- **Não use `git add .`** sem conferir o `git status`. Adicione os arquivos pelo nome.

### Regras que ninguém pode quebrar

1. **Os dois repositórios são públicos.** Tudo que entra neles, inclusive no histórico, fica visível para qualquer pessoa, para sempre. Apagar no commit seguinte não resolve.
2. **Nunca** commitar `.env`, chaves, tokens, senhas reais, bancos (`*.mv.db`), logs, URLs de produção com credenciais ou redações reais de alunos.
3. **Nunca** colar a chave da OpenAI em chat de IA, issue, PR ou código. IAs: não leiam nem imprimam o conteúdo de `.env`.
4. **Nunca apagar nem desativar teste para deixar a CI verde.** Teste quebrado se corrige, no teste ou no código, com justificativa no PR.
5. **Testes automatizados não chamam a OpenAI de verdade.** Use mock ou servidor HTTP falso.
6. **`V1__init.sql` só pode ser editado pela tarefa 1.5**, e só até o primeiro deploy em produção. Depois disso, toda mudança de schema vira nova migração (`V2__...sql`).
7. Use só dados fictícios em desenvolvimento e testes: nomes inventados e redações escritas pela equipe.
8. Faça edições pontuais. Não reescreva arquivos inteiros nem reformate o que a tarefa não pede.
9. Fique dentro da tarefa. Achou outro problema? Anote no PR, em "Achados fora do escopo", e não corrija junto.

### Instruções para assistentes de IA

- Responda e comente o código em **português do Brasil**. Siga os nomes já existentes (em português, ex.: `RedacaoService.enviarTexto`).
- Antes de editar, **abra e leia os arquivos listados na tarefa**. As linhas citadas são de 25/11/2026 e podem ter mudado.
- Ao terminar, rode o comando de verificação da tarefa e mostre a saída real. Nunca diga que passou sem ter rodado.
- Se a tarefa depende de outra ainda não concluída (campo "Depende de"), avise o humano antes de começar.
- Se este documento contradizer o código atual, o código vence: relate a divergência ao humano.

---

# Parte B — Contexto

### O que é o CorreX

Plataforma web de **correção de redações assistida por IA** para professores de idiomas. O professor cadastra turmas, alunos e critérios de avaliação (pesos que somam 100) e envia a redação, como texto colado ou arquivo PDF/DOCX/TXT. A IA (OpenAI) corrige e dá nota por critério; o professor revisa, ajusta e marca como enviada ao aluno.

Nasceu como projeto acadêmico da UNIFACISA (Desafio UNIFACISA, orientador Daniel Abella). **Agora o objetivo é uso real:** um piloto com 2 professores (o Thayslan é um deles), com redações reais de alunos.

### Repositórios e equipe

| Repo | GitHub (público) | Pasta local do Thayslan | Porta |
|---|---|---|---|
| Backend | [`thayslancosta/correx-api`](https://github.com/thayslancosta/correx-api) | `~/Documents/correx-api` | 8080 |
| Frontend | [`thayslancosta/frontendCorrex`](https://github.com/thayslancosta/frontendCorrex) | `~/Documents/"Frontend Little bull"/correx` (o caminho tem espaços; use aspas) | 5173 |

Equipe no GitHub: `thayslancosta` (dono), `ramontrindd`, `PedroRodrigues17`, `lubrias`, `vtvicent`.

Fora do escopo: `thayslancosta/correx-frontend` (demo pública antiga; existe uma cópia local dentro da pasta do frontend, em `correx-frontend-demo/`, ignorada pelo Git) e o DevClass.

### Stack

- **Backend:** Java 17, Spring Boot 3.2.4, Spring Security + JWT (jjwt, HS256, expira em 24 h), Spring Data JPA / Hibernate 6.4, Flyway 9.22, H2 (dev) e PostgreSQL (prod), WebClient (WebFlux) para a OpenAI, PDFBox 3, Apache POI (DOCX), Bucket4j (limite de requisições), springdoc (Swagger), Lombok. **Não há `mvnw`**: use `mvn`. Compila também com JDK 21 (alvo 17).
- **Frontend:** React 19, Vite 8, React Router 7, Axios. Sem biblioteca de UI: CSS próprio com variáveis (`--bg-base`, `--accent` etc.), tema claro/escuro, fonte Plus Jakarta Sans. ESLint 9. Sem testes automatizados.
- **IA:** OpenAI `gpt-4o-mini` para correção e `gpt-4o` (Vision) como OCR de PDF escaneado (cada página vira PNG em base64).

### Arquitetura do backend (`src/main/java/com/correx`)

```
config/       SecurityConfig, WebConfig (CORS duplicado), RateLimitConfig, OpenApiConfig,
              DevProfessorBootstrap (cria professor@correx.com / 123456 só no perfil dev), DataSeeder
controller/   Auth, Turma, Aluno, Criterio, Redacao, Dashboard   →  /api/v1/...
service/      regras de negócio; RedacaoService orquestra extração de texto + IA
service/openai/OpenAiService   chamadas HTTP à OpenAI (correção e OCR)
security/     JwtService, JwtAuthenticationFilter, RateLimitFilter, CustomUserDetailsService
entity/       Professor, Turma, Aluno, CriterioAvaliacao, Redacao, Correcao, NotaCriterio, ErroEncontrado
exception/    GlobalExceptionHandler + exceções de negócio
resources/db/migration/V1__init.sql   única migração
```

- Isolamento entre professores: todo acesso a dados filtra pelo `professorId` do usuário logado. Conferido nos services; está correto.
- Ciclo da redação (`StatusRedacao`): `PROCESSANDO → CORRIGIDA → REVISADA → ENVIADA`, ou `ERRO` se a IA falhar. Hoje o `ERRO` se perde (tarefa 2.1).

### Endpoints (`/api/v1`)

| Recurso | Endpoints |
|---|---|
| auth | `POST /auth/login`, `POST /auth/register` (desligado em produção) |
| turmas | `GET/POST /turmas`, `GET/PUT/DELETE /turmas/{id}`, `GET /turmas/opcoes`, `GET /turmas/{id}/alunos/opcoes` |
| alunos | `GET/POST /alunos`, `GET/PUT/DELETE /alunos/{id}` |
| criterios | `GET/POST /criterios`, `PUT/DELETE /criterios/{id}`, `GET /criterios/validacao` (pesos devem somar 100) |
| redacoes | `POST /redacoes/texto`, `POST /redacoes/upload` (multipart: `arquivo`, `idioma`, `turmaId`, `alunoId` ou `nomeAluno`), `GET /redacoes`, `GET /redacoes/{id}`, `GET /redacoes/aluno/{alunoId}`, `PUT /redacoes/{id}/revisao`, `PUT /redacoes/{id}/recorrigir`, `PUT /redacoes/{id}/enviar` |
| dashboard | `GET /dashboard` |

Abertos sem login: login, register, `/actuator/health`, Swagger (só em dev) e console H2 (só em dev).

### Como rodar e verificar

**Backend**
```bash
cd ~/Documents/correx-api
cp .env.example .env     # preencha JWT_SECRET (>= 32 caracteres) e a OPENAI_API_KEY de dev
export $(grep -v '^#' .env | xargs)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# Verificação:
mvn -B test
```
Login de desenvolvimento: `professor@correx.com` / `123456` (criado automaticamente só no perfil `dev`). Swagger em dev: http://localhost:8080/swagger-ui/index.html

**Frontend**
```bash
cd ~/Documents/"Frontend Little bull"/correx
cp .env.example .env     # VITE_API_BASE_URL=http://localhost:8080/api/v1
npm ci
npm run dev
# Verificação:
npm run lint && npm run build
```

### Variáveis de ambiente

| Variável | Onde | Observação |
|---|---|---|
| `JWT_SECRET` | back | Mínimo 32 bytes aleatórios: `openssl rand -base64 48` |
| `OPENAI_API_KEY` | back | Em dev: chave do projeto OpenAI `correx-dev`, que o Thayslan entrega por canal privado. Em prod: chave do projeto `correx-piloto`, só no painel da hospedagem. |
| `DATABASE_URL`, `DB_USER`, `DB_PASS` | back (prod) | JDBC, ex.: `jdbc:postgresql://host/db?sslmode=require` |
| `CORS_ORIGINS` | back (prod) | URL exata do frontend publicado, sem barra no fim |
| `APP_REGISTRATION_ENABLED` | back (prod) | `false` no piloto. Ligar só temporariamente para criar contas (tarefa 4.3) |
| `VITE_API_BASE_URL` | front | **Vai embutida no JavaScript público.** Nunca coloque segredo em variável `VITE_*` |

Os projetos `correx-dev` e `correx-piloto` na OpenAI têm limite de US$ 5/mês cada. Ao atingir o limite, as chamadas falham com erro 429.

---

# Parte C — Diagnóstico

### Como foi feito

Em 25/11/2026 o backend foi compilado e **iniciado de verdade** (perfil `dev`, H2 novo, chave OpenAI falsa), e a API recebeu 23 requisições de teste (tabela abaixo). O frontend passou por `npm ci`, ESLint e build numa cópia limpa. O código dos dois repositórios foi lido. Na mesma data, o Thayslan rodou `mvn -B test` e as verificações do frontend no Mac (Fase 0).

### Resultado geral

| Verificação | Resultado |
|---|---|
| Backend compila e sobe (dev/H2) | OK, em cerca de 4 s |
| Testes do backend | **78 testes: 70 passam, 8 falham** (5 em `OpenAiServiceTest`, 3 em `GlobalExceptionHandlerTest`) |
| Backend em PostgreSQL | **Não testado e com alta chance de falhar** (tarefa 1.5) |
| Frontend `npm run build` | OK |
| Frontend `npm run lint` | **7 erros** no código versionado. No Mac aparecem 119 porque o ESLint também lê a pasta local `correx-frontend-demo/` (tarefa 1.3) |
| Dependências do frontend | `npm ci` aponta **16 vulnerabilidades** (1 baixa, 3 moderadas, 12 altas) (tarefa 1.7) |
| Testes do frontend | Não existem |
| CI | Não existe |
| Segredos no histórico Git | Nenhuma chave real encontrada nos dois repositórios |

### Teste da API (backend real, IA falsa)

| # | Cenário | Esperado | Obtido | Tarefa |
|---|---|---|---|---|
| 1 | `GET /turmas` sem token | 401 | **403** | 2.2 |
| 2 | Login com credenciais de dev | 200 + token | 200 | ok |
| 3 | `POST /auth/register` anônimo | bloqueado | era 200; **já corrigido na Fase 0** | ok |
| 4-6 | Criar/listar turma, criar aluno | 201/200 | 201/200 | ok |
| 7 | Dashboard | coerente | `alunosAtivos: 0` com 1 aluno ativo | Parte F |
| 8-9 | Critérios padrão e validação (soma 100) | 200 | 200 | ok |
| 10 | Enviar redação por texto com a IA falhando | redação salva com status `ERRO` | 502 e **a redação some** | 2.1 |
| 11 | Listar redações depois da falha | 1 redação em `ERRO` | **lista vazia** | 2.1 |
| 12 | Upload `.txt` com a IA falhando | idem | 502 e a redação some | 2.1 |
| 13 | Upload de arquivo falso `.pdf` | 400 | 400 | ok |
| 14 | Upload maior que 10 MB | 413 | **500** | 2.4 |
| 15 | Swagger | só em dev | **já desligado em prod na Fase 0** | ok |
| 16 | Console H2 em dev | abre | 403, servlet não inicia | Parte F |
| 17 | `/actuator/health` | 200 | 200 | ok |
| 18 | JSON malformado | 400 | **500** | 2.4 |
| 19 | Token inválido | 401 | **403** | 2.2 |
| 20 | Turma inexistente | 404 | 404 | ok |
| 21 | CORS de origem não autorizada | bloqueado | 403 | ok |
| 22 | 8 logins errados em 1 minuto | 429 a partir do 6º | nunca bloqueava; **já corrigido na Fase 0** | ok |
| 23 | Rota inexistente | 404 | **500** | 2.4 |

### Os problemas mais graves

**Impedem o piloto:**
- **Schema incompatível com PostgreSQL** (1.5): `V1__init.sql` usa `CLOB`, tipo que não existe no PostgreSQL, e as entidades usam `@Lob String` (no Postgres, o Hibernate 6 mapeia para `oid`). O deploy deve falhar na migração ou na validação do schema.
- **Redação perdida quando a IA falha** (2.1): `RedacaoService` é `@Transactional` na classe inteira. A exceção desfaz tudo, inclusive o status `ERRO`, e o professor perde o texto enviado. Além disso, a chamada HTTP à OpenAI (até 30 s por correção, 120 s por página de OCR) segura uma conexão de banco aberta.
- **Sessão expirada trava o usuário** (2.2 + 2.5): sem token ou com token expirado, o backend responde 403, e o frontend só desloga em 401. Depois de 24 h o professor fica preso numa tela que só dá erro. Também não há proteção de rotas no frontend.
- **8 testes falhando e nenhuma CI** (1.1 + 1.2): sem isso, ninguém sabe se o código de outra pessoa (ou da IA dela) quebrou algo.
- **Sem configuração de deploy** (3.6): `server.port` está fixo em 8080, e hospedagens como o Render informam a porta em `PORT`.

**Importantes para o piloto:**
- Falhas da OpenAI (chave inválida, limite, timeout) viram todas "resposta inválida" (2.3).
- JSON inválido, upload grande e rota inexistente devolvem 500 (2.4).
- Nenhum limite de gasto por professor; o OCR usa `gpt-4o`, mais caro (3.2).
- Os logs gravam trechos do texto das redações e o corpo das respostas da OpenAI (3.3).
- As telas de Correção, Revisão e Redações do aluno perdem o conteúdo ao recarregar a página (2.6).
- Uma correção pode levar mais de 1 minuto (servidor acordando + IA) sem aviso na tela (3.7).

---

# Parte D — Plano

## Visão geral das fases

| Fase | Objetivo | Tarefas | Termina quando |
|---|---|---|---|
| **0. Preparação** | Segurança e organização antes de chamar a equipe | Feita pelo Thayslan | ✅ Concluída em 25/11/2026 |
| **1. Fundação** | Ter onde trabalhar com segurança: CI, testes verdes, lint zerado, banco igual ao de produção | 1.1 a 1.8 | CI verde nos dois repositórios e API rodando em PostgreSQL |
| **2. Bugs críticos** | Corrigir o que perde dados ou trava o usuário | 2.1 a 2.8 | Roteiro de testes rodado em dev sem bug bloqueante |
| **3. Preparação para uso real** | Custo, privacidade, desempenho e servidor | 3.1 a 3.9 | Container de 512 MB rodando com PostgreSQL e IA real medida |
| **4. Deploy do piloto** | Publicar, criar as contas e liberar | 4.1 a 4.7 | Checklist da Parte E todo marcado |

**Sugestão de divisão (5 pessoas):** Infra: 1 · Backend: 2 (uma em testes e erros, outra em IA e persistência) · Frontend: 1 · QA e Docs: 1, que também revisa PRs do ponto de vista do usuário. O Thayslan revisa todos os PRs.

---

## Fase 0 — Preparação ✅ concluída

Feita pelo Thayslan em 25/11/2026. Não há tarefa da equipe aqui; esta seção registra o que já mudou.

**Código (já na `main`):**
- [`correx-api#2`](https://github.com/thayslancosta/correx-api/pull/2): `/auth/register` responde 403 com o cadastro desligado, que é o padrão em produção (`APP_REGISTRATION_ENABLED=false`); Swagger e api-docs desligados em produção; o limite de 5 tentativas/min por IP em login e cadastro passou a funcionar e responde 429 com JSON; ajustes em `docker-compose.yml` (healthcheck com o usuário real), `.env.example` e `.gitignore`.
- [`frontendCorrex#5`](https://github.com/thayslancosta/frontendCorrex/pull/5): `.gitignore` passa a ignorar `.env`, `.env.*` (exceto `.env.example`), `correx-frontend-demo/` e o ZIP da demo.
- `correx-api#1` e `frontendCorrex#4`: removidos relatórios e notas internas antigas; README do frontend consolidado.
- A pasta local de Ansible (teste abandonado, nunca versionado) foi apagada.

**Contas e acesso:**
- Chaves antigas da OpenAI revogadas. Criados os projetos `correx-dev` e `correx-piloto`, cada um com limite de US$ 5/mês.
- Regra `protege-main` ativa nos dois repositórios: PR com 1 aprovação, sem push direto, force push ou exclusão. Status checks obrigatórios ficam para depois da tarefa 1.1.
- Repositórios públicos; histórico verificado sem segredos.
- Colaboradores convidados com permissão Write.

**Pendências do Thayslan (não bloqueiam a Fase 1):**
- [ ] Conferir se todos aceitaram os convites (no `correx-api`, os 4 convites estavam pendentes; no `frontendCorrex`, o de `vtvicent`)
- [ ] Guardar as chaves `correx-dev` e `correx-piloto` no gerenciador de senhas e entregar a `correx-dev` à equipe por canal privado
- [ ] Atribuir as tarefas da Fase 1
- [ ] Ligar os status checks obrigatórios na regra `protege-main` quando a tarefa 1.1 terminar
- [ ] Definir os dados de alunos do piloto: só primeiro nome ou apelido, sem e-mail, e avisar os alunos que as redações passam pela API da OpenAI (necessário antes da Fase 4)
- [ ] Decidir a licença dos repositórios
- [ ] Decidir o que fazer com a chave `devclass-local` (fora do escopo do CorreX)

---

## Fase 1 — Fundação

**Objetivo:** dar à equipe uma base segura para trabalhar. **Termina quando:** a CI roda verde nos dois repositórios e a API sobe em PostgreSQL.

| Tarefa | Área | O que é | Depende de |
|---|---|---|---|
| 1.1 | Infra | CI no GitHub Actions | — |
| 1.2 | Backend | Deixar os 78 testes verdes | 1.1 |
| 1.3 | Frontend | Zerar o lint e remover código morto | 1.1 |
| 1.4 | Infra | PostgreSQL local | — |
| 1.5 | Backend | Schema compatível com PostgreSQL | 1.4 |
| 1.6 | Frontend | Uma única configuração de URL da API | — |
| 1.7 | Frontend | Tratar as 16 vulnerabilidades do npm | 1.3 |
| 1.8 | QA | Roteiro de testes e redações de exemplo | — |

#### 1.1 · [Infra] CI no GitHub Actions nos dois repositórios
- **Arquivos:** `correx-api/.github/workflows/ci.yml` e `frontendCorrex/.github/workflows/ci.yml` (novos). Não mexer em `.github/modernize/`.
- **Problema:** não existe CI; o código gerado pela IA de cada pessoa só é rodado pelo autor.
- **O que fazer:**
  - Backend: rodar em `pull_request` e em `push` na `main`; `actions/setup-java` (temurin 17, `cache: maven`); `mvn -B verify`. Sem `OPENAI_API_KEY` no workflow; os testes não podem depender dela. Publicar `target/surefire-reports` como artifact quando falhar.
  - Frontend: `actions/setup-node` (Node 22, `cache: npm`); `npm ci`; `npm run lint`; `npm run build` com `VITE_API_BASE_URL=https://example.invalid/api/v1`.
- **Pronto quando:** os workflows rodam em todo PR. O do backend mostra exatamente as 8 falhas conhecidas até a 1.2 corrigir; o do frontend mostra os 7 erros de lint até a 1.3 corrigir. Avise o Thayslan para ligar os status checks obrigatórios.

#### 1.2 · [Backend] Deixar os 78 testes verdes, sem apagar nenhum
- **Arquivos:** `src/test/java/com/correx/service/openai/OpenAiServiceTest.java`, `src/test/java/com/correx/exception/GlobalExceptionHandlerTest.java`, `src/main/java/com/correx/service/openai/OpenAiService.java`, `src/main/java/com/correx/exception/GlobalExceptionHandler.java`
- **Causas das 8 falhas:**
  1. `OpenAiServiceTest` (5 erros, mesma causa): o `@InjectMocks` tenta construir o `OpenAiService`, cujo construtor chama `webClientBuilder.baseUrl(...)` com o builder `null`, o que dá NullPointerException.
  2. `GlobalExceptionHandlerTest.testHandleValidationException`: uso errado do Mockito (`WrongTypeOfReturnValue`). O teste encadeia `when(exception.getBindingResult().getFieldErrors())` sobre outro stub.
  3. `testHandleExternalServiceExceptionFor429` e `...Generic`: **bug real no código**. Em `determinarMensagemSegura`, os casos 429, 401, timeout e 5xx só são tratados se a mensagem contiver "OCR" ou "PDF", e a mensagem genérica não contém "serviço externo".
- **O que fazer:**
  - Tornar a URL base configurável: `@Value("${openai.base-url:https://api.openai.com/v1}")`. No teste, construir o service manualmente com um `WebClient.builder()` real apontando para um servidor falso (`MockWebServer`, dependência de teste `com.squareup.okhttp3:mockwebserver`). Não usar `@InjectMocks` com esse construtor.
  - Item 2: criar `BindingResult bindingResult = mock(BindingResult.class)`, stubar `getFieldErrors()` e depois `when(exception.getBindingResult()).thenReturn(bindingResult)`.
  - Item 3: em `determinarMensagemSegura`, tratar 429, 401, timeout e 5xx fora do ramo de OCR/PDF, e fazer a mensagem genérica conter "serviço externo".
- **Pronto quando:** `mvn -B test` com 0 falhas e 0 erros, 78 testes ou mais, nenhuma chamada real à OpenAI e CI verde.

#### 1.3 · [Frontend] Zerar o lint e remover código morto
- **Erros atuais (`npx eslint .`):**
  | Arquivo:linha | Regra | Problema |
  |---|---|---|
  | `src/components/Sidebar.jsx:41` | no-unused-vars | `Icon` não usado |
  | `src/pages/Sidebar.jsx:91` | no-unused-vars | `Icon` não usado |
  | `src/components/Toast.jsx:1` | no-unused-vars | `useEffect` importado sem uso |
  | `src/components/Toast.jsx:66` | react-refresh/only-export-components | o arquivo exporta componente e função |
  | `src/context/ThemeContext.jsx:22` | react-refresh/only-export-components | o hook é exportado junto com o Provider |
  | `src/pages/RedacoesAluno.jsx:16` | react-hooks/set-state-in-effect | `setState` síncrono dentro de `useEffect` |
  | `src/services/authService.js:7` | no-useless-catch | `try/catch` que só relança |
- **O que fazer:**
  - Em `eslint.config.js`, acrescentar `correx-frontend-demo` aos diretórios ignorados (`globalIgnores`). Hoje o lint mostra 119 erros em quem tem essa pasta local.
  - **Apagar `src/pages/Sidebar.jsx`**: é uma cópia morta, já que todas as páginas importam `components/Sidebar.jsx`.
  - Para o `react-refresh`, mover hooks e funções para arquivos próprios (ex.: `context/useTheme.js`, `components/toastUtils.js`).
  - Em `RedacoesAluno.jsx`, calcular o valor direto na renderização em vez de copiar para o estado dentro do efeito.
- **Pronto quando:** `npm run lint` sem erros (inclusive no Mac do Thayslan, que tem a pasta da demo), `npm run build` OK e a navegação continua igual.

#### 1.4 · [Infra] PostgreSQL local para desenvolvimento
- **Arquivos:** `docker-compose.yml`, `README.md` do backend
- **Problema:** dev usa H2 e produção usa PostgreSQL; o schema nunca foi validado no PostgreSQL.
- **O que fazer:** documentar no README como subir só o banco (`docker compose up -d db`) e rodar a API no perfil `prod` contra ele (`DATABASE_URL=jdbc:postgresql://localhost:5432/correx`). Usar a mesma versão de PostgreSQL da hospedagem (o Neon usa 16 ou 17).
- **Pronto quando:** qualquer pessoa da equipe sobe o PostgreSQL e a API seguindo só o README. (A API só vai subir de fato depois da 1.5.)

#### 1.5 · [Backend] Schema compatível com PostgreSQL
- **Depende de:** 1.4 · **Única tarefa autorizada a editar `V1__init.sql`.**
- **Arquivos:** `src/main/resources/db/migration/V1__init.sql`, `entity/Redacao.java`, `entity/Correcao.java`, `entity/ErroEncontrado.java`
- **Problema:** `V1__init.sql` declara como `CLOB` as colunas `redacoes.texto_original`, `redacoes.texto_editado`, `correcoes.comentario_geral`, `correcoes.feedback_extra`, `correcoes.json_resposta_ia`, `erros_encontrados.texto_original` e `erros_encontrados.texto_corrigido`. O PostgreSQL não tem esse tipo, e as entidades usam `@Lob String`, que no PostgreSQL vira `oid`.
- **O que fazer:**
  - Como ainda não existe banco de produção, alterar o próprio `V1__init.sql`, trocando `CLOB` por `TEXT` (aceito pelo PostgreSQL e pelo H2 2.x).
  - Nas entidades, trocar `@Lob` por `@Column(columnDefinition = "TEXT")`, mantendo `nullable = false` onde já existe.
  - Avisar a equipe para apagar o banco H2 local (`data/correxdb.mv.db`), porque o checksum da V1 muda.
  - Recomendado: teste de integração com Testcontainers (`org.testcontainers:postgresql`) que sobe o contexto Spring no PostgreSQL. Roda na CI, que tem Docker.
- **Pronto quando:** a API sobe no perfil `prod` contra o PostgreSQL local sem erro do Flyway nem do Hibernate; criar redação, correção e revisão funciona no PostgreSQL; em `dev` com H2 continua funcionando.

#### 1.6 · [Frontend] Uma única configuração de URL da API
- **Arquivos:** `src/services/api.js`, `src/services/authService.js`, `.env.example`
- **Problema:** `authService` usa o `axios` direto, com a URL duplicada. Os dois arquivos caem em `http://localhost:8080` se a variável faltar, e um build de produção sem a variável sai "funcionando" apontando para localhost.
- **O que fazer:** `authService` passa a usar a instância `api`. Ler `VITE_API_BASE_URL` num lugar só; em build de produção sem a variável, falhar o build (checagem em `vite.config.js`).
- **Pronto quando:** há uma única ocorrência de `VITE_API_BASE_URL` em `src/`, e o build de produção sem a variável falha com mensagem clara.

#### 1.7 · [Frontend] Tratar as vulnerabilidades do npm
- **Depende de:** 1.3 (para não misturar mudanças no mesmo arquivo `package-lock.json`)
- **Problema:** `npm ci` aponta 16 vulnerabilidades (1 baixa, 3 moderadas, 12 altas).
- **O que fazer:** rodar `npm audit`; aplicar `npm audit fix` (**sem** `--force`); para o que sobrar, verificar se o pacote vai para o navegador ou se é só ferramenta de desenvolvimento (ESLint, Vite) e listar no PR o que foi corrigido e o que ficou, com o motivo.
- **Pronto quando:** nenhuma vulnerabilidade alta em pacote que vai para o navegador; build e lint OK; o PR explica as restantes.

#### 1.8 · [QA] Roteiro de testes e redações de exemplo
- **Arquivos:** `correx-api/docs/ROTEIRO_TESTES.md` e `correx-api/docs/amostras/` (novos)
- **Roteiro mínimo:** login e logout; sessão expirada (editar o token no `localStorage`); cadastrar, editar e apagar turma, aluno e critério; critérios somando 100 e diferente de 100; redação por texto; upload de TXT, DOCX, PDF digital e **PDF escaneado**; arquivo inválido, `.doc` e arquivo maior que 10 MB; revisão (alterar nota e comentário); recorreção; enviar ao aluno; dashboard; dois professores (um não vê os dados do outro); F5 em cada tela; tema claro e escuro; celular.
- **Amostras:** 5 redações **fictícias** em inglês e espanhol (níveis A2, B1 e avançado), escritas pela equipe, e 1 PDF escaneado de uma redação manuscrita fictícia. **Nenhuma redação real.**
- **Pronto quando:** o roteiro tem passos, resultado esperado e uma coluna para marcar o resultado de cada rodada.

---

## Fase 2 — Bugs críticos

**Objetivo:** corrigir o que perde dados, trava o usuário ou devolve erro sem sentido. **Termina quando:** o roteiro da 1.8 roda em dev (H2 e PostgreSQL) sem bug bloqueante.

| Tarefa | Área | O que é | Depende de |
|---|---|---|---|
| 2.1 | Backend | Redação perdida quando a IA falha | — |
| 2.2 | Backend | Responder 401, e não 403, sem login | — |
| 2.3 | Backend | Classificar corretamente os erros da OpenAI | — |
| 2.4 | Backend | Erros do cliente devolvendo 500 | — |
| 2.5 | Frontend | Rotas protegidas e sessão expirada | 2.2 |
| 2.6 | Frontend | Telas que quebram ao recarregar (F5) | — |
| 2.7 | Frontend | Mostrar as mensagens de erro do backend | 2.3, 2.4 |
| 2.8 | QA | Rodar o roteiro em dev | 2.1 a 2.7 |

#### 2.1 · [Backend] Redação perdida quando a IA falha
- **Arquivo:** `service/RedacaoService.java` (`@Transactional` na linha 24; métodos `enviarTexto`, `enviarArquivo`, `recorrigirRedacao`)
- **Problema:** ao falhar a IA, o código faz `saved.setStatus(ERRO); save(saved); throw ex;`, mas a exceção desfaz a transação inteira e a redação desaparece (teste da API, linhas 10 a 12).
- **O que fazer:**
  - Separar em 3 passos, cada um com transação própria: (1) salvar a redação com `PROCESSANDO`; (2) chamar a OpenAI **fora de transação**; (3) salvar o resultado (`CORRIGIDA`) **ou** o `ERRO` numa transação nova. Use `TransactionTemplate`, ou mova os passos 1 e 3 para outro service com `@Transactional(propagation = REQUIRES_NEW)`. Atenção: chamar um método `@Transactional` da própria classe não abre transação nova.
  - Continuar devolvendo 502 ao cliente, mas a redação fica consultável com status `ERRO`, e `PUT /redacoes/{id}/recorrigir` funciona para ela.
  - Em `recorrigirRedacao`, uma falha não pode apagar a correção anterior.
- **Pronto quando:** com a IA indisponível, `POST /redacoes/texto` devolve 502 **e** `GET /redacoes` lista a redação em `ERRO`; com a IA de volta, recorrigir funciona. Teste de integração novo (`@SpringBootTest` + H2 + `OpenAiService` mockado lançando `ExternalServiceException`) comprova isso.

#### 2.2 · [Backend] Responder 401, e não 403, sem login
- **Arquivos:** `config/SecurityConfig.java` e, se preciso, `security/JwtAuthenticationFilter.java`
- **Problema:** sem `AuthenticationEntryPoint`, o Spring Security responde 403 para token ausente, inválido ou expirado. O frontend só desloga em 401.
- **O que fazer:** `http.exceptionHandling(e -> e.authenticationEntryPoint(...).accessDeniedHandler(...))`, devolvendo 401 e 403 em JSON no formato `ApiErrorResponse` (`code: "UNAUTHORIZED"` / `"FORBIDDEN"`). Conferir que o `JwtAuthenticationFilter` não lança exceção com token expirado ou malformado; ele só deve deixar de autenticar.
- **Pronto quando:** sem token, token malformado e token expirado dão 401 com JSON; login válido continua 200. Comprovado por teste `@SpringBootTest` + `MockMvc` **com os filtros ligados** (os testes de controller atuais usam `addFilters = false` e não cobrem segurança).

#### 2.3 · [Backend] Classificar corretamente os erros da OpenAI
- **Arquivos:** `service/openai/OpenAiService.java` (`corrigirRedacao`, `chamarChatGpt`), `exception/ErrorMessages.java`
- **Problema:** `corrigirRedacao` captura qualquer `Exception` e relança tudo como "resposta inválida", inclusive chave inválida (401), limite (429) e timeout. A exceção de "tentativas esgotadas" do Reactor também cai no `catch` genérico.
- **O que fazer:** relançar `ExternalServiceException` sem embrulhar de novo; usar "resposta inválida" só para falha ao ler o JSON; tratar `Exceptions.isRetryExhausted`. Preferir um código de erro (enum) na exceção em vez de comparar textos. Mensagens sugeridas: 401 → "Configuração da IA inválida. Avise o administrador."; 429 → "Limite de uso da IA atingido. Tente novamente em alguns minutos."; timeout → "A correção demorou demais. Tente novamente."
- **Pronto quando:** um teste para cada tipo de falha (401, 429, timeout, JSON inválido), usando o servidor falso criado na 1.2.

#### 2.4 · [Backend] Erros do cliente devolvendo 500
- **Arquivo:** `exception/GlobalExceptionHandler.java`
- **Problema:** o `@ExceptionHandler(Exception.class)` captura tudo (teste da API, linhas 14, 18 e 23).
- **O que fazer:** handlers para `HttpMessageNotReadableException` → 400; `MaxUploadSizeExceededException` → 413 ("Arquivo maior que 10 MB"); `NoResourceFoundException` → 404; `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException` e `MissingServletRequestPartException` → 400; `HttpRequestMethodNotSupportedException` → 405. Atenção: `ErroResponse` **não** é duplicata de `ApiErrorResponse`; é o DTO dos erros gramaticais da correção. Não mexer.
- **Pronto quando:** nenhum desses cenários devolve 500; um teste por handler.

#### 2.5 · [Frontend] Rotas protegidas e sessão expirada
- **Depende de:** 2.2
- **Arquivos:** `src/App.jsx`, `src/services/api.js`, novo `src/components/ProtectedRoute.jsx`
- **Problema:** todas as rotas (`/dashboard`, `/turmas` etc.) abrem sem login.
- **O que fazer:** `ProtectedRoute` que manda para `/login` se não houver token ou se o `exp` do JWT já passou (decodificar o payload em base64 basta; não precisa de biblioteca). No interceptor do Axios, ao receber 401: limpar o storage e ir para `/login` com o aviso "Sua sessão expirou". Quem já está logado e abre `/login` vai para `/dashboard`.
- **Pronto quando:** sem login, qualquer rota interna leva ao login; token expirado ou adulterado no `localStorage` leva ao login com aviso; o login normal continua funcionando.

#### 2.6 · [Frontend] Telas que quebram ao recarregar (F5)
- **Arquivos:** `src/App.jsx`, `src/pages/Correcao.jsx` (`location.state?.redacaoId`), `src/pages/Revisao.jsx` (`redacaoId`, `draft`), `src/pages/RedacoesAluno.jsx` (`alunoId`, `alunoNome`) e todos os `navigate(...)` que levam a essas telas
- **O que fazer:** colocar os ids na URL (`/correcao/:redacaoId`, `/revisao/:redacaoId`, `/alunos/:alunoId/redacoes`) e buscar os dados na API com `useParams`. O rascunho da revisão (`draft`) pode continuar no state, mas a tela precisa funcionar sem ele.
- **Pronto quando:** abrir ou recarregar diretamente a URL de uma correção, revisão ou lista de redações do aluno mostra os dados certos.

#### 2.7 · [Frontend] Mostrar as mensagens de erro do backend
- **Depende de:** 2.3 e 2.4
- **Arquivos:** `Upload.jsx`, `Correcao.jsx`, `Revisao.jsx` e as chamadas em `services/`
- **O que fazer:** exibir `error.response.data.message` no Toast; mensagens próprias para 429 (limite), 413 (arquivo grande), 502 (IA indisponível; a redação ficou salva com erro e pode ser recorrigida) e erro de rede (servidor fora do ar). Em `Upload.jsx` (linha ~414), trocar `accept=".pdf,.doc,.docx,.txt"` por `accept=".pdf,.docx,.txt"`, porque o backend recusa `.doc`.
- **Pronto quando:** cada cenário mostra uma mensagem compreensível, nunca um erro genérico.

#### 2.8 · [QA] Rodar o roteiro em dev
- **Depende de:** 2.1 a 2.7
- **O que fazer:** rodar o roteiro da 1.8 com H2 e com PostgreSQL. Cada bug vira uma issue `QA-BUG-nn` com passos para reproduzir, esperado × obtido e captura de tela.
- **Pronto quando:** nenhuma issue bloqueante aberta. **Isso encerra a Fase 2.**

---

## Fase 3 — Preparação para uso real

**Objetivo:** controlar custo, proteger os dados dos alunos e preparar a aplicação para o servidor gratuito. **Termina quando:** o container roda com 512 MB contra PostgreSQL e a correção com IA real foi medida (tempo e custo).

| Tarefa | Área | O que é | Depende de |
|---|---|---|---|
| 3.1 | Infra | Escolher a hospedagem | — |
| 3.2 | Backend | Limite de gasto com a OpenAI | — |
| 3.3 | Backend | Tirar dados de alunos dos logs | — |
| 3.4 | Backend | Timeouts e formato da resposta da IA | — |
| 3.5 | Backend | Testes de segurança e isolamento entre professores | — |
| 3.6 | Infra | Dockerfile e configuração de produção | 3.1 |
| 3.7 | Frontend | Espera longa: correção e servidor dormindo | 3.1 |
| 3.8 | Frontend | Testes automatizados mínimos | — |
| 3.9 | QA | IA real em homologação | 3.2, 3.4, 3.6 |

#### 3.1 · [Infra] Escolher a hospedagem mais barata
- **Arquivo:** `correx-api/docs/DEPLOY.md` (novo) · **Decisão final:** Thayslan
- **Opções (preços de 25/11/2026; confirmar antes de decidir):**
  - **Render Free (API em Docker):** grátis; dorme após 15 min sem tráfego e leva cerca de 1 min para acordar; 512 MB de RAM e 0,1 CPU; disco efêmero (nada pode ser salvo em arquivo).
  - **Render Postgres Free:** expira em 30 dias. **Não usar.**
  - **Neon Free (PostgreSQL):** grátis, 0,5 GB, sem expiração. **Recomendado.**
  - **Frontend:** Render Static Site (grátis, não dorme) ou Vercel Hobby (grátis, uso não comercial).
  - **Para a API não dormir:** Render Starter, cerca de US$ 7/mês.
- **Recomendação:** Render Free (API) + Neon Free (banco) + Render Static Site ou Vercel (front). Infraestrutura a R$ 0; o único custo variável é a OpenAI.
- **Pronto quando:** `DEPLOY.md` traz a escolha, os limites aceitos e o custo mensal estimado, aprovado pelo Thayslan. **Pode ser decidida antes, durante a Fase 2.**

#### 3.2 · [Backend] Limite de gasto com a OpenAI
- **Arquivos:** `application.yml`, `service/RedacaoService.java`
- **Problema:** não há teto por professor, e o OCR de PDF escaneado faz uma chamada a `gpt-4o` por página.
- **O que fazer:** limite configurável de correções por professor por dia (`app.ia.limite-diario-por-professor`, sugestão: 30), contando as redações criadas no dia; ao atingir, responder 429 com mensagem clara, **sem** chamar a OpenAI. Limitar as páginas do OCR (sugestão: 5). Avaliar se `gpt-4o-mini` dá conta do OCR (`openai.ocr-model`).
- **Pronto quando:** limite atingido dá 429 sem chamada à OpenAI; PDF acima do limite de páginas dá 400 com mensagem.

#### 3.3 · [Backend] Tirar dados de alunos dos logs
- **Arquivos:** `service/openai/OpenAiService.java` (logs com `previewTexto` nas linhas ~132, 260, 263, 269, 350 e 367; `ex.getResponseBodyAsString()` em `chamarChatGpt`), `service/FileService.java`, `application.yml` (`show-sql`)
- **O que fazer:** em produção, nunca registrar trechos de redação, nomes de alunos ou o corpo das respostas da IA. Rebaixar para `debug` (desligado em prod) ou remover. Registrar só ids, tamanhos e códigos de erro.
- **Pronto quando:** no perfil `prod`, o roteiro completo não deixa texto de redação no log (um `grep` por uma frase da redação de teste não encontra nada).

#### 3.4 · [Backend] Timeouts e formato da resposta da IA
- **Arquivo:** `service/openai/OpenAiService.java`
- **O que fazer:** enviar `"response_format": {"type": "json_object"}` na correção (hoje o JSON é extraído de texto livre); tornar configuráveis o timeout da correção (30 s fixos, curto para `max_tokens: 2000`) e o do OCR (120 s fixos); falhar rápido, com mensagem clara, se `openai.api-key` estiver vazia.
- **Pronto quando:** uma redação fictícia longa (cerca de 400 palavras) é corrigida com a IA real; chave vazia gera erro claro sem chamada HTTP.

#### 3.5 · [Backend] Testes de segurança e isolamento entre professores
- **O que fazer:** testes de integração com filtros ligados provando que: (a) o professor A não lê, edita nem apaga turma, aluno, critério ou redação do professor B (deve dar 404); (b) cadastro desligado dá 403; (c) a 6ª tentativa de login no mesmo minuto dá 429.
- **Pronto quando:** os testes passam na CI.

#### 3.6 · [Infra] Dockerfile e configuração de produção
- **Depende de:** 3.1
- **Arquivos:** `Dockerfile`, `.dockerignore`, `src/main/resources/application.yml`
- **O que fazer:**
  - `server.port: ${PORT:8080}` (o Render informa a porta em `PORT`).
  - JVM com limite de memória: `ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]`.
  - Rodar sem root: `adduser -D app` + `USER app`.
  - `.dockerignore` excluindo `target/`, `data/`, `.env` e `*.log`.
  - No perfil `prod`, `spring.jpa.open-in-view: false`, testando se nada quebra.
- **Pronto quando:** `docker build` funciona; o container sobe no perfil `prod` contra o PostgreSQL local com `docker run -m 512m`, e `/actuator/health` responde 200.

#### 3.7 · [Frontend] Espera longa: correção e servidor dormindo
- **Depende de:** 3.1
- **Problema:** na hospedagem gratuita, a primeira requisição após 15 min parado leva cerca de 1 min, e uma correção com OCR pode demorar mais. O Axios não tem timeout.
- **O que fazer:** timeout explícito (sugestão: 180 s para correção e 60 s para o resto); durante a correção, aviso na tela ("Corrigindo… pode levar até 2 minutos"); botão de envio desabilitado para impedir duplo clique, que gera duas cobranças na OpenAI; no login, aviso se a primeira resposta demorar ("Acordando o servidor…").
- **Pronto quando:** com o backend lento (simule com o throttling de rede do DevTools), a tela informa o que acontece e não deixa reenviar.

#### 3.8 · [Frontend] Testes automatizados mínimos
- **O que fazer:** instalar Vitest + Testing Library; script `npm test`; testes para `ProtectedRoute`, o interceptor de 401 e o formulário de login; incluir `npm test` na CI.
- **Pronto quando:** `npm test` passa localmente e na CI.

#### 3.9 · [QA] IA real em homologação
- **Depende de:** 3.2, 3.4 e 3.6
- **O que fazer:** com a chave `correx-dev`, rodar as amostras da 1.8 no container de 512 MB e registrar, para cada uma: tempo de correção, custo aproximado (painel de uso da OpenAI), qualidade das notas por critério e qualidade do OCR.
- **Pronto quando:** `ROTEIRO_TESTES.md` traz uma tabela com tempo e custo médio por correção. **Isso encerra a Fase 3.** Com esse custo, o Thayslan confere se o limite de US$ 5/mês do projeto `correx-piloto` basta.

---

## Fase 4 — Deploy do piloto

**Objetivo:** publicar, criar as contas dos 2 professores e liberar o uso. **Termina quando:** todos os itens da Parte E estão marcados.

| Tarefa | Área | O que é | Depende de |
|---|---|---|---|
| 4.1 | Infra | Segredos e variáveis na hospedagem | — |
| 4.2 | Frontend | Build de produção apontando para a API real | — |
| 4.3 | Infra | Deploy e criação das contas | 4.1, 4.2 |
| 4.4 | Infra | Backup do banco | 4.3 |
| 4.5 | Infra | Monitoramento mínimo | 4.3 |
| 4.6 | Docs | READMEs prontos para recrutadores | 4.3 |
| 4.7 | QA | Aceite final (go/no-go) | 4.3 a 4.6 |

#### 4.1 · [Infra] Segredos e variáveis na hospedagem
- **Executa:** Thayslan, com a pessoa de Infra
- **O que fazer:** cadastrar no painel da hospedagem `JWT_SECRET` (novo, só de produção), `OPENAI_API_KEY` (`correx-piloto`), `DATABASE_URL` (JDBC com `sslmode=require`), `DB_USER`, `DB_PASS`, `CORS_ORIGINS`, `SPRING_PROFILES_ACTIVE=prod` e `APP_REGISTRATION_ENABLED=false`. No frontend, só `VITE_API_BASE_URL`.
- **Pronto quando:** nenhum segredo aparece em arquivo do repositório, e o `JWT_SECRET` de produção é diferente do de dev.

#### 4.2 · [Frontend] Build de produção apontando para a API real
- **O que fazer:** configurar o projeto na hospedagem com o `VITE_API_BASE_URL` de produção e adicionar o redirecionamento de SPA (`vercel.json` com `rewrites` para `/index.html`, ou a regra de rewrite do Render Static Site). Sem isso, o F5 em `/turmas` dá 404.
- **Pronto quando:** a URL pública abre, faz login na API de produção e o F5 funciona em qualquer rota.

#### 4.3 · [Infra] Deploy e criação das contas
- **Depende de:** 4.1 e 4.2
- **O que fazer:**
  1. Publicar a API e o frontend; conferir `/actuator/health`.
  2. Ligar `APP_REGISTRATION_ENABLED=true`, cadastrar as 2 contas de professor, **desligar** de novo e reiniciar.
  3. Confirmar que `POST /auth/register` responde 403 e que o Swagger não abre em produção.
- **Pronto quando:** os 2 professores fazem login na URL pública e o cadastro está fechado.

#### 4.4 · [Infra] Backup do banco
- **O que fazer:** documentar e testar um `pg_dump` (manual semanal, ou GitHub Action agendada que guarda o dump como artifact). **Atenção:** artifacts de repositório público podem ser baixados por qualquer pessoa. Se usar Action, guarde o dump fora do GitHub. Testar a restauração num PostgreSQL local.
- **Pronto quando:** um backup foi restaurado localmente com sucesso.

#### 4.5 · [Infra] Monitoramento mínimo
- **O que fazer:** documentar onde ver os logs e, opcionalmente, configurar um monitor de uptime gratuito no `/actuator/health`. Um ping a cada menos de 15 min mantém o Render Free acordado e consome as 750 h grátis do mês; para um único serviço cabe. Registrar a decisão em `DEPLOY.md`.

#### 4.6 · [Docs] READMEs prontos para recrutadores
- **Backend:** créditos da equipe, link para o frontend, status "piloto em produção", aviso de que o cadastro é fechado em produção; remover textos de preparação antigos e o aviso de "chave a revogar" (as chaves antigas já foram revogadas).
- **Frontend:** nomes da equipe, capturas de tela reais, link da URL pública, status de piloto; trocar a frase "latest reported local check".
- **Pronto quando:** o Thayslan aprova os dois READMEs.

#### 4.7 · [QA] Aceite final (go/no-go)
- **Depende de:** 4.3 a 4.6
- **O que fazer:** rodar o roteiro inteiro **na URL de produção** com dados fictícios, conferir a Parte E e apagar os dados de teste.
- **Pronto quando:** a Parte E está toda marcada. **O piloto está liberado.**

---

# Parte E — Pronto para o piloto (go/no-go)

Todos precisam estar marcados para liberar o uso pelos professores:

- [x] Fase 0 concluída: chaves antigas revogadas, chaves novas com limite de gasto, `main` protegida
- [ ] CI verde nos dois repositórios, com os status checks obrigatórios ligados
- [ ] API rodando em PostgreSQL, com a migração aplicada do zero (1.5)
- [ ] Falha da IA não perde a redação, e ela pode ser recorrigida (2.1)
- [ ] Sessão expirada leva ao login (2.2 + 2.5)
- [ ] Cadastro fechado e Swagger desligado, testados na URL pública (4.3)
- [ ] Limite diário de correções por professor ativo (3.2)
- [ ] Logs de produção sem texto de redação (3.3)
- [ ] F5 funciona em todas as telas (2.6 + 4.2)
- [ ] Backup restaurado com sucesso (4.4)
- [ ] Dados de alunos do piloto definidos e alunos avisados (pendência do Thayslan na Fase 0)
- [ ] Roteiro executado na produção sem bug bloqueante (4.7)
- [ ] Os 2 professores fizeram login e corrigiram uma redação fictícia na produção

---

# Parte F — Depois do piloto

### Melhorias sem prazo
- CORS configurado duas vezes (`WebConfig` e `SecurityConfig`); manter só o do `SecurityConfig`.
- Console H2 não inicia em dev (403 / `Servlet.init() threw exception`): corrigir ou remover a rota.
- Dashboard: `alunosAtivos` volta 0 com aluno ativo (a regra atual parece contar só alunos com redação; confirmar com o Thayslan); `calcularMediaGeral` carrega todas as redações com `PageRequest.of(0, Integer.MAX_VALUE)` (trocar por consulta agregada); `TurmaService` faz contagens em laço (N+1).
- `RateLimitFilter` confia no primeiro valor de `X-Forwarded-For`, que o cliente pode falsificar, e o mapa de limites cresce sem fim.
- Atualizar o Spring Boot 3.2.4, fora do suporte open source, para a versão 3.x suportada mais recente, junto com o Flyway.
- Correção assíncrona (fila + status consultado periodicamente) no lugar de uma requisição HTTP longa.
- Token em cookie `HttpOnly` em vez de `localStorage`.
- Testes de ponta a ponta automatizados (Playwright) com o roteiro da 1.8.
- Usar PostgreSQL também em dev e remover o H2.
- Troca de senha e recuperação de acesso.
- Exportar a correção para o aluno (PDF ou link).

### Limites deste diagnóstico
- O PostgreSQL não foi testado; o problema da tarefa 1.5 vem da leitura do SQL e das entidades. Trate como "muito provável" até a 1.4 confirmar.
- A OpenAI real não foi chamada (chave falsa). Com a chave real, o fluxo de correção ainda não foi validado; a 3.9 cobre isso.
- O frontend foi verificado por lint, build e leitura do código, mas não navegado no navegador; a 2.8 cobre isso.
- Preços e limites de hospedagem mudam; confirme nas páginas oficiais na tarefa 3.1.
