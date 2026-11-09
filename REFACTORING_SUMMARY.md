# Refatoração Completa do Fluxo de Upload de PDF - Resumo de Mudanças

## 📋 Resumo Executivo
Implementada refatoração completa do fluxo de upload de PDF com suporte a:
- ✅ Heurística robusta para detectar PDFs digitais vs escaneados
- ✅ Suporte para OCR de múltiplas páginas
- ✅ Melhorias significativas em logs e tratamento de erros
- ✅ Configurações separadas para OCR
- ✅ Testes unitários cobrindo todos os casos de uso
- ✅ Remoção de prefixo mágico "PDF_IMAGEM_BASE64:" por definição limpa

---

## 🔍 Causa Raiz Identificada

**Problema Principal:**
- O fluxo antigo tinha uma abordagem "tudo ou nada" com prefixo mágico para identificar tipo de PDF
- Heurística de detecção baseada apenas em `texto.length() > 50` era frágil
- OCR apenas da primeira página de PDFs escaneados
- Mensagens de erro genéricas escondiam problemas reais
- Falta de logs contextualizados

**Solução:**
- Substituído por arquitetura limpa com record `PdfExtractionResult`
- Heurística aprimorada verificando caracteres reais + presença de palavras
- Suporte completo para todas as páginas em OCR
- Sistema de mensagens de erro estruturado
- Logs detalhados em cada etapa

---

## 📁 Arquivos Alterados

### 1. **src/main/java/com/correx/exception/ErrorMessages.java**
- ✅ Adicionadas mensagens específicas para PDF:
  - `PDF_EMPTY`, `PDF_CORRUPT`, `PDF_TEXT_EXTRACTION_FAILED`, `PDF_OCR_FAILED`
- ✅ Adicionadas mensagens específicas para OCR:
  - `OPENAI_OCR_ERROR`, `OPENAI_OCR_INVALID_RESPONSE`
- **Impacto:** Melhor comunicação de erros ao cliente

### 2. **src/main/resources/application.yml**
- ✅ Adicionadas propriedades específicas para OCR:
  ```yaml
  openai:
    ocr-model: gpt-4o-mini
    ocr-max-tokens: 4000
  ```
- **Impacto:** Configuração independente para OCR permite ajustes sem afetar correção de redações

### 3. **src/main/java/com/correx/service/PdfExtractionResult.java** (NOVO)
- ✅ Record que encapsula resultado de extração de PDF
- ✅ Suporta tanto String (texto digital) quanto List<String> (imagens para OCR)
- ✅ Métodos helper: `getTextAsString()` e `getImagesAsBase64List()`
- ✅ Enum `ExtractionType`: DIGITAL_TEXT, SCANNED_OCR, OTHER_FORMAT
- **Impacto:** Elimina prefixo mágico, estrutura limpa e type-safe

### 4. **src/main/java/com/correx/service/FileService.java** (REFATORADO)
**Melhorias principais:**
- ✅ Novo método `extrairTextoPdfCompleto()` com suporte completo
- ✅ Heurística aprimorada `isTextoDigitalUtil()`:
  - Verifica caracteres não-whitespace (mínimo 100)
  - Valida presença de palavras reais (>2 caracteres)
  - Evita falsos positivos com lixo/caracteres quebrados
- ✅ Novo método `extrairTextoViaOcr()` para múltiplas páginas:
  - Renderiza cada página em imagem (DPI 150)
  - Converte para base64
  - Retorna lista para OCR posterior
- ✅ Logs detalhados:
  - Número de páginas
  - Tipo de extração (digital/OCR)
  - Caracteres reais extraídos
- ✅ Tratamento de erro melhorado com ErrorMessages
- **Impacto:** PDFs escaneados com múltiplas páginas são agora processados corretamente

### 5. **src/main/java/com/correx/service/openai/OpenAiService.java** (REFATORADO)
**Melhorias principais:**
- ✅ Adicionadas propriedades `ocrModel` e `ocrMaxTokens`
- ✅ Novo método `extrairTextoDeImagensOcr(List<String>)`:
  - Processa cada página separadamente
  - Concatena textos na ordem correta
  - Timeout aumentado para 120s (OCR é lento)
  - Trata erros de forma granular
- ✅ Método privado `extrairTextoDeImagemOcr()` com prompt aprimorado:
  - Instruções rigorosas para OCR
  - Exigências explícitas: transcrição fiel, sem correção, sem resumo, etc.
  - Tratamento de texto ilegível com [ilegível]
- ✅ Logs estruturados por página
- ✅ Método `chamarChatGpt()` genérico com retry automático
- ✅ Melhor tratamento de erros HTTP (401, 429, 5xx)
- ✅ Método antigo `extrairTextoDeImagem()` deprecado para compatibilidade
- **Impacto:** OCR é agora confiável para múltiplas páginas com instruções claras

### 6. **src/main/java/com/correx/service/RedacaoService.java** (REFATORADO)
**Melhorias principais:**
- ✅ Adicionado logger
- ✅ Novo método `extrairTextoDoArquivo()`:
  - Routing inteligente entre PDF e outros formatos
  - Compatibilidade com novo fluxo
- ✅ Novo método `extrairTextoPdfComOcr()`:
  - Usa novo `extrairTextoPdfCompleto()` do FileService
  - Switch-case para diferentes tipos de extração
  - Se digital: retorna direto
  - Se escaneado: envia para OCR com `extrairTextoDeImagensOcr()`
  - Logs de contexto
- ✅ Método helper `obterExtensao()` para parsing de filename
- ✅ Removido prefixo mágico "PDF_IMAGEM_BASE64:"
- **Impacto:** Fluxo claro, sem gambiarras, fácil de entender e manter

### 7. **src/main/java/com/correx/exception/GlobalExceptionHandler.java** (REFATORADO)
**Melhorias principais:**
- ✅ Adicionado logger em nível estruturado
- ✅ Novo método `determinarMensagemSegura()`:
  - Retorna mensagens diferentes para cliente
  - Preserva erros detalhados no log
  - Casos específicos: OCR timeout, rate limit, falha de autenticação, etc.
- ✅ Tratamento específico para `ExternalServiceException`:
  - Log detalhado com stack trace
  - Mensagem segura para cliente
- ✅ Log de validação com contador de erros
- ✅ Log de erro interno com URI da requisição
- **Impacto:** Melhor experiência do usuário + debugging mais fácil

### 8. **Testes (NOVOS)**
- ✅ `src/test/java/com/correx/service/FileServiceTest.java`:
  - Arquivo vazio
  - Arquivo muito grande
  - Sem extensão/formato inválido
  - Extração de TXT
  - Metadados
  - PDF vazio/corrompido
  
- ✅ `src/test/java/com/correx/service/openai/OpenAiServiceTest.java`:
  - Lista vazia/null de imagens
  - Processamento de múltiplas imagens
  - Compatibilidade de método deprecado
  - Validação de critérios
  
- ✅ `src/test/java/com/correx/service/PdfExtractionResultTest.java`:
  - Criação com texto digital
  - Criação com OCR múltiplas páginas
  - Exceções ao acessar tipo errado
  - Resultado para outros formatos
  - Páginas parcialmente processadas
  
- ✅ `src/test/java/com/correx/exception/GlobalExceptionHandlerTest.java`:
  - ResourceNotFoundException
  - BusinessException
  - UnauthorizedException
  - ExternalServiceException (OCR, timeout, 429, 401, genérico)
  - MethodArgumentNotValidException
  - Exception genérica

---

## 🧪 Como Testar Manualmente

### Teste 1: PDF com Texto Digital
```bash
# 1. Crie ou use um PDF com texto extraível (normal)
# 2. Faça upload via:
curl -X POST http://localhost:8080/api/v1/redacoes/upload \
  -F "arquivo=@documento_digital.pdf" \
  -F "idioma=português" \
  -F "alunoId=1" \
  -H "Authorization: Bearer <token>"

# Resultado esperado: ✅ Texto extraído diretamente (sem OCR), redação corrigida
# Logs esperados: "Texto digital extraído com sucesso. Páginas: X, Caracteres: Y"
```

### Teste 2: PDF Escaneado (1 página)
```bash
# 1. Use um PDF escaneado/imagem (sem texto extraível)
# 2. Faça upload via:
curl -X POST http://localhost:8080/api/v1/redacoes/upload \
  -F "arquivo=@redacao_escaneada.pdf" \
  -F "idioma=português" \
  -F "alunoId=1" \
  -H "Authorization: Bearer <token>"

# Resultado esperado: ✅ PDF convertido em imagem, OCR via OpenAI, redação corrigida
# Logs esperados:
#   "PDF escaneado detectado. Páginas: 1"
#   "Iniciando OCR de 1 página(s)..."
#   "OCR completo. Total de caracteres: Z"
```

### Teste 3: PDF Escaneado (Múltiplas Páginas)
```bash
# 1. Use um PDF escaneado com 3+ páginas
# 2. Faça upload via:
curl -X POST http://localhost:8080/api/v1/redacoes/upload \
  -F "arquivo=@redacao_escaneada_3p.pdf" \
  -F "idioma=português" \
  -F "alunoId=1" \
  -H "Authorization: Bearer <token>"

# Resultado esperado: ✅ Todas as 3 páginas convertidas, OCR de cada, textos concatenados
# Logs esperados:
#   "PDF escaneado detectado. Páginas: 3"
#   "Iniciando OCR de 3 página(s)..."
#   "Processando OCR da página 1 de 3"
#   "Processando OCR da página 2 de 3"
#   "Processando OCR da página 3 de 3"
#   "OCR completo de 3 página(s). Total de caracteres: Z"
```

### Teste 4: PDF Corrompido/Ilegível
```bash
# 1. Crie um arquivo "PDF" inválido (ex: .zip renomeado para .pdf)
# 2. Faça upload via:
curl -X POST http://localhost:8080/api/v1/redacoes/upload \
  -F "arquivo=@fake.pdf" \
  -F "idioma=português" \
  -F "alunoId=1" \
  -H "Authorization: Bearer <token>"

# Resultado esperado: ❌ Erro 400
# Resposta JSON:
# {
#   "status": 400,
#   "errorCode": "BUSINESS_RULE_VIOLATION",
#   "message": "Arquivo PDF corrompido ou ilegível"
# }
```

### Teste 5: Arquivo Vazio
```bash
# 1. Crie arquivo vazio: touch empty.pdf
# 2. Faça upload via:
curl -X POST http://localhost:8080/api/v1/redacoes/upload \
  -F "arquivo=@empty.pdf" \
  -F "idioma=português" \
  -F "alunoId=1" \
  -H "Authorization: Bearer <token>"

# Resultado esperado: ❌ Erro 400
# Resposta JSON:
# {
#   "status": 400,
#   "errorCode": "BUSINESS_RULE_VIOLATION",
#   "message": "Arquivo PDF vazio ou sem conteúdo extraível"
# }
```

### Teste 6: Formato Não Suportado
```bash
# 1. Tente upload de arquivo .exe ou outro formato:
curl -X POST http://localhost:8080/api/v1/redacoes/upload \
  -F "arquivo=@programa.exe" \
  -F "idioma=português" \
  -F "alunoId=1" \
  -H "Authorization: Bearer <token>"

# Resultado esperado: ❌ Erro 400
# Resposta JSON:
# {
#   "status": 400,
#   "errorCode": "BUSINESS_RULE_VIOLATION",
#   "message": "Formato de arquivo não suportado. Use PDF, DOC, DOCX ou TXT."
# }
```

---

## 🚀 Executando Testes Unitários

```bash
# Todos os testes
mvn test

# Apenas FileService
mvn test -Dtest=FileServiceTest

# Apenas OpenAiService
mvn test -Dtest=OpenAiServiceTest

# Apenas PdfExtractionResult
mvn test -Dtest=PdfExtractionResultTest

# Apenas GlobalExceptionHandler
mvn test -Dtest=GlobalExceptionHandlerTest

# Com cobertura
mvn clean test jacoco:report
```

---

## 📊 Métricas de Melhoria

| Aspecto | Antes | Depois |
|---------|-------|--------|
| Páginas de OCR suportadas | 1 | Ilimitadas |
| Heurística de detecção | String length > 50 | Chars + palavras reais |
| Logs contextualizados | Mínimos | Completos + nºpáginas |
| Mensagens de erro | Genéricas | Específicas por tipo |
| Teste de PDF digital vs OCR | Não | Sim |
| Tratamento de erro OCR | Genérico | Por página + retry |
| Compatibilidade código legado | 100% | 100% (com deprecation) |

---

## 🔐 Segurança

- ✅ API key não é logada em logs públicos
- ✅ Mensagens de erro para cliente são seguras (sem expor internals)
- ✅ Stack trace completo apenas em logs de servidor
- ✅ Validação rigorosa de tamanho de arquivo (10MB máx)
- ✅ Timeout para chamadas externas (30s correção, 120s OCR)

---

## 📝 Notas Importantes

1. **Compatibilidade Retroativa**
   - Método `extrairTextoDeImagem()` deprecado mas funcional
   - Prefixo mágico removido internamente, não quebra APIs públicas
   - Toda integração existente continua funcionando

2. **Performance**
   - DPI renderização: 150 (balanço qualidade/performance)
   - Max tokens OCR: 4000 (espaço para múltiplas páginas)
   - Retry automático apenas para erros 5xx (não para 4xx)

3. **Próximos Passos Opcionais**
   - Implementar cache de OCR para mesma imagem
   - Adicionar endpoint de preview de extração (antes de gravar)
   - Suporte para outros formatos (PPTX, ODP)
   - Dashboard de estatísticas (% PDF digital vs OCR)
   - Webhook para notificação do OCR completo
