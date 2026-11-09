package com.correx.exception;

/**
 * Constantes de mensagens de erro
 * Centraliza todas as mensagens de erro da aplicação para fácil manutenção
 */
public final class ErrorMessages {

    private ErrorMessages() {
        throw new AssertionError("Classe utilitária não instanciável");
    }

    // AUTH ERRORS
    public static final String EMAIL_NOT_FOUND = "Email não encontrado no sistema";
    public static final String INVALID_PASSWORD = "Senha inválida";
    public static final String EMAIL_ALREADY_EXISTS = "Email já cadastrado no sistema";
    public static final String UNAUTHORIZED_ACCESS = "Acesso não autorizado";
    public static final String INVALID_TOKEN = "Token JWT inválido ou expirado";
    public static final String TOKEN_MISSING = "Token JWT ausente no header Authorization";

    // RESOURCE ERRORS
    public static final String TURMA_NOT_FOUND = "Turma não encontrada";
    public static final String ALUNO_NOT_FOUND = "Aluno não encontrado";
    public static final String CRITERIO_NOT_FOUND = "Critério de avaliação não encontrado";
    public static final String REDACAO_NOT_FOUND = "Redação não encontrada";
    public static final String PROFESSOR_NOT_FOUND = "Professor não encontrado";
    public static final String CORRECAO_NOT_FOUND = "Correção não encontrada";

    // BUSINESS RULE ERRORS
    public static final String INVALID_CRITERIO_WEIGHT = "Peso do critério deve estar entre 1 e 100";
    public static final String CRITERIO_WEIGHTS_INVALID_SUM = "A soma dos pesos dos critérios deve ser exatamente 100%";
    public static final String FILE_TOO_LARGE = "Arquivo excede o tamanho máximo permitido (10MB)";
    public static final String INVALID_FILE_FORMAT = "Formato de arquivo não suportado. Use PDF, DOC, DOCX ou TXT";
    public static final String EMPTY_FILE = "Arquivo vazio ou sem conteúdo extraível";
    public static final String INVALID_ESSAY_TEXT = "Texto da redação não pode estar vazio";

    // PDF ERRORS
    public static final String PDF_EMPTY = "Arquivo PDF vazio ou sem conteúdo extraível";
    public static final String PDF_CORRUPT = "Arquivo PDF corrompido ou ilegível";
    public static final String PDF_TEXT_EXTRACTION_FAILED = "Falha ao extrair texto do PDF";
    public static final String PDF_OCR_FAILED = "Falha ao realizar OCR do PDF escaneado";
    public static final String PDF_UNSUPPORTED = "Tipo de PDF não suportado";

    // EXTERNAL SERVICE ERRORS
    public static final String OPENAI_API_ERROR = "Erro ao comunicar com OpenAI API";
    public static final String OPENAI_INVALID_RESPONSE = "Resposta inválida da OpenAI";
    public static final String OPENAI_TIMEOUT = "Timeout ao aguardar resposta da OpenAI";
    public static final String OPENAI_OCR_ERROR = "Erro ao realizar OCR via OpenAI Vision";
    public static final String OPENAI_OCR_INVALID_RESPONSE = "Resposta inválida do OCR da OpenAI";
    public static final String OPENAI_OCR_INSUFFICIENT_TEXT = "OCR retornou texto insuficiente para uma redação";
    public static final String EXTERNAL_SERVICE_ERROR = "Erro ao chamar serviço externo";

    // CONCURRENCY ERRORS
    public static final String OPTIMISTIC_LOCK_ERROR = "Recurso foi modificado por outro usuário. Por favor, recarregue e tente novamente";
    public static final String CONCURRENT_MODIFICATION = "Modificação concorrente detectada";

    // VALIDATION ERRORS
    public static final String NAME_REQUIRED = "Nome é obrigatório";
    public static final String EMAIL_REQUIRED = "Email é obrigatório";
    public static final String EMAIL_INVALID_FORMAT = "Formato de email inválido";
    public static final String PASSWORD_TOO_SHORT = "Senha deve ter pelo menos 6 caracteres";
    public static final String PASSWORD_REQUIRED = "Senha é obrigatória";

    // RATE LIMIT ERRORS
    public static final String RATE_LIMIT_EXCEEDED = "Muitas requisições. Tente novamente mais tarde";
    public static final String RATE_LIMIT_LOGIN = "Muitas tentativas de login. Aguarde antes de tentar novamente";

    // GENERIC ERRORS
    public static final String INTERNAL_SERVER_ERROR = "Erro interno do servidor";
    public static final String BAD_REQUEST = "Requisição inválida";
    public static final String FORBIDDEN = "Acesso proibido";
}
