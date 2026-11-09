package com.correx.service.openai;

import com.correx.entity.CriterioAvaliacao;
import com.correx.exception.ErrorMessages;
import com.correx.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);
    private static final int OCR_BATCH_TIMEOUT_SECONDS = 120; // Timeout maior para OCR
    private static final int MIN_OCR_NON_WHITESPACE_CHARS = 100;
    private static final int MIN_OCR_VALID_WORDS = 20;
    private static final int OCR_PREVIEW_LENGTH = 200;
    private static final String OCR_PRIMARY_PROMPT = """
        Transcreva todo o texto visível desta página exatamente como aparece.
        Mantenha a ordem de leitura e as quebras de parágrafo.
        Não resuma, não traduza, não corrija, não explique e não adicione comentários.
        Se alguma palavra estiver ilegível, escreva [ilegível].
        Retorne somente a transcrição.
        """;
    private static final String OCR_FALLBACK_PROMPT = """
        Leia a imagem e extraia apenas o texto nela contido.
        Preserve parágrafos e ordem de leitura.
        Não responda com explicações.
        Não use markdown.
        Retorne apenas o texto transcrito.
        """;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private int maxTokens;

    @Value("${openai.temperature}")
    private double temperature;

    // OCR specific configuration
    @Value("${openai.ocr-model:gpt-4o-mini}")
    private String ocrModel;

    @Value("${openai.ocr-max-tokens:4000}")
    private int ocrMaxTokens;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
            .baseUrl("https://api.openai.com/v1")
            .build();
        this.objectMapper = objectMapper;
    }

    public OpenAiCorrecaoResult corrigirRedacao(String texto, String idioma, List<CriterioAvaliacao> criterios) {
        String prompt = montarPrompt(texto, idioma, criterios);

        try {
            String response = chamarChatGpt(prompt, model, maxTokens, temperature);
            String jsonLimpo = extrairJson(response);
            return objectMapper.readValue(jsonLimpo, OpenAiCorrecaoResult.class);
        } catch (Exception e) {
            logger.error("Erro ao processar resposta da correção da IA", e);
            throw new ExternalServiceException(ErrorMessages.OPENAI_INVALID_RESPONSE, e);
        }
    }

    /**
     * Extrai texto de múltiplas imagens de PDF escaneado via OCR
     * Processa cada página e concatena os textos mantendo a ordem
     */
    public String extrairTextoDeImagensOcr(List<String> base64Images) {
        if (base64Images == null || base64Images.isEmpty()) {
            throw new ExternalServiceException(ErrorMessages.PDF_OCR_FAILED);
        }

        StringBuilder textoCompleto = new StringBuilder();
        int totalPages = base64Images.size();

        logger.info("Iniciando OCR em {} página(s)...", totalPages);

        for (int pageIndex = 0; pageIndex < base64Images.size(); pageIndex++) {
            try {
                String base64Image = base64Images.get(pageIndex);
                int pageNumber = pageIndex + 1;

                logger.debug("Processando OCR da página {} de {}", pageNumber, totalPages);

                String textoExtraido = extrairTextoDeImagemOcr(base64Image);

                if (textoExtraido != null && !textoExtraido.trim().isEmpty()) {
                    if (textoCompleto.length() > 0) {
                        textoCompleto.append("\n\n"); // Separador entre páginas
                    }
                    textoCompleto.append(textoExtraido);
                    logger.debug("Página {} OCR bem-sucedida. Caracteres: {}", pageNumber, textoExtraido.length());
                } else {
                    logger.warn("Página {} OCR retornou texto vazio", pageNumber);
                }

            } catch (ExternalServiceException ex) {
                logger.error("Falha no OCR da página {} de {}: {}", pageIndex + 1, totalPages, ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                logger.error("Erro inesperado no OCR da página {} de {}", pageIndex + 1, totalPages, ex);
                throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_ERROR, ex);
            }
        }

        if (textoCompleto.length() == 0) {
            throw new ExternalServiceException(ErrorMessages.PDF_EMPTY);
        }

        if (!isTextoOcrUtil(textoCompleto.toString())) {
            logger.error("OCR retornou texto insuficiente para o documento inteiro. Preview: {}", previewTexto(textoCompleto.toString()));
            throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_INSUFFICIENT_TEXT);
        }

        logger.info("OCR completo de {} página(s). Total de caracteres: {}", totalPages, textoCompleto.length());
        return textoCompleto.toString();
    }

    /**
     * Extrai texto de uma única imagem via OCR da OpenAI Vision
     */
    private String extrairTextoDeImagemOcr(String base64Imagem) {
        logger.debug("Enviando imagem para OCR via OpenAI Vision...");
        String textoExtraido = tentarOcrComPrompt(base64Imagem, OCR_PRIMARY_PROMPT, "principal");
        if (textoExtraido != null) {
            return textoExtraido;
        }

        logger.warn("OCR principal falhou ou recusou. Tentando fallback com prompt alternativo.");
        textoExtraido = tentarOcrComPrompt(base64Imagem, OCR_FALLBACK_PROMPT, "fallback");
        if (textoExtraido != null) {
            return textoExtraido;
        }

        throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_INSUFFICIENT_TEXT);
    }

    /**
     * Chamada genérica para ChatGPT com retry automático
     */
    private String chamarChatGpt(String prompt, String usedModel, int usedMaxTokens, double usedTemperature) {
        Map<String, Object> requestBody = Map.of(
            "model", usedModel,
            "messages", List.of(
                Map.of("role", "system", "content", "Você é um professor experiente de idiomas que corrige redações de alunos."),
                Map.of("role", "user", "content", prompt)
            ),
            "max_tokens", usedMaxTokens,
            "temperature", usedTemperature
        );

        try {
            return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.max(1).filter(this::isRetryable))
                .map(this::extractContent)
                .block();
        } catch (WebClientResponseException ex) {
            logger.error("Erro HTTP {} ao chamar OpenAI: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ExternalServiceException(ErrorMessages.OPENAI_API_ERROR + ": " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            logger.error("Erro ao chamar OpenAI", ex);
            throw new ExternalServiceException(ErrorMessages.OPENAI_API_ERROR, ex);
        }
    }

    private String montarPrompt(String texto, String idioma, List<CriterioAvaliacao> criterios) {
        StringBuilder sb = new StringBuilder();
        sb.append("Por favor, corrija a seguinte redação em ").append(idioma).append(":\n\n");
        sb.append("---REDAÇÃO---\n").append(texto).append("\n---FIM DA REDAÇÃO---\n\n");

        sb.append("Critérios de avaliação com pesos:\n");
        for (CriterioAvaliacao criterio : criterios) {
            sb.append("- ").append(criterio.getNome()).append(" (peso: ").append(criterio.getPeso()).append("%)\n");
        }

        sb.append("\nRegras de pontuação:\n");
        sb.append("- A nota final deve ser um número entre 0 e 100.\n");
        sb.append("- A nota de cada critério deve ser um número entre 0 e o peso do critério.\n");
        sb.append("- A soma das notas dos critérios deve resultar na nota final.\n");

        sb.append("\nRetorne a resposta em JSON puro (sem markdown) com a seguinte estrutura:\n");
        sb.append("{\n");
        sb.append("  \"notaFinal\": <número entre 0 e 100>,\n");
        sb.append("  \"comentarioGeral\": \"<comentário detalhado>\",\n");
        sb.append("  \"notas\": [\n");
        sb.append("    {\"nomeCriterio\": \"<nome>\", \"peso\": <número>, \"nota\": <número entre 0 e o peso do critério>}\n");
        sb.append("  ],\n");
        sb.append("  \"erros\": [\n");
        sb.append("    {\"categoria\": \"<tipo de erro>\", \"textoOriginal\": \"<texto errado>\", \"textoCorrigido\": \"<texto correto>\"}\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("Responda em ").append(idioma).append(".");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
            throw new ExternalServiceException(ErrorMessages.OPENAI_INVALID_RESPONSE);
        } catch (ClassCastException e) {
            throw new ExternalServiceException(ErrorMessages.OPENAI_INVALID_RESPONSE, e);
        }
    }

    private String extrairJson(String response) {
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");

        if (jsonStart == -1 || jsonEnd == -1) {
            throw new ExternalServiceException(ErrorMessages.OPENAI_INVALID_RESPONSE);
        }

        return response.substring(jsonStart, jsonEnd + 1);
    }

    private boolean isRetryable(Throwable ex) {
        return ex instanceof WebClientResponseException && 
               ((WebClientResponseException) ex).getStatusCode().is5xxServerError();
    }

    private String tentarOcrComPrompt(String base64Imagem, String prompt, String tentativa) {
        try {
            String responseJson = chamarOcr(prompt, base64Imagem);
            String textoExtraido = extrairTextoOcrDaResposta(responseJson);

            logger.info("OCR {} da página concluído. Caracteres: {}, Preview: {}", tentativa, textoExtraido.length(), previewTexto(textoExtraido));

            if (isRespostaDeRecusa(textoExtraido)) {
                logger.warn("OCR {} retornou recusa do modelo. Preview: {}", tentativa, previewTexto(textoExtraido));
                return null;
            }

            String textoSanitizado = sanitizarTextoOcr(textoExtraido);
            if (!isTextoOcrUtil(textoSanitizado)) {
                logger.warn("OCR {} retornou texto insuficiente. Preview: {}", tentativa, previewTexto(textoSanitizado));
                return null;
            }

            return textoSanitizado;
        } catch (ExternalServiceException ex) {
            if (ErrorMessages.OPENAI_OCR_INVALID_RESPONSE.equals(ex.getMessage())) {
                logger.warn("OCR {} retornou resposta inválida", tentativa);
                return null;
            }
            throw ex;
        }
    }

    private String chamarOcr(String prompt, String base64Imagem) {
        Map<String, Object> imageContent = Map.of(
            "type", "image_url",
            "image_url", Map.of(
                "url", "data:image/png;base64," + base64Imagem,
                "detail", "high"
            )
        );

        Map<String, Object> textContent = Map.of(
            "type", "text",
            "text", prompt
        );

        Map<String, Object> message = Map.of(
            "role", "user",
            "content", List.of(textContent, imageContent)
        );

        Map<String, Object> requestBody = Map.of(
            "model", ocrModel,
            "messages", List.of(message),
            "max_tokens", ocrMaxTokens,
            "temperature", 0
        );

        try {
            String responseJson = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(OCR_BATCH_TIMEOUT_SECONDS))
                .retryWhen(Retry.max(1).filter(this::isRetryable))
                .block();

            if (responseJson == null) {
                throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_INVALID_RESPONSE);
            }

            return responseJson;
        } catch (WebClientResponseException ex) {
            logger.error("Erro HTTP {} na chamada OCR: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ExternalServiceException(ErrorMessages.OPENAI_API_ERROR + " (HTTP " + ex.getStatusCode() + ")", ex);
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Erro inesperado ao chamar OCR da OpenAI", ex);
            throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_ERROR, ex);
        }
    }

    private String extrairTextoOcrDaResposta(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                String errorMsg = errorNode.path("message").asText("Erro desconhecido");
                logger.error("Erro da OpenAI no OCR: {}", errorMsg);
                throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_ERROR + ": " + errorMsg);
            }

            JsonNode choicesNode = root.path("choices");
            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                logger.error("Resposta OCR sem choices válidos. Corpo: {}", previewTexto(responseJson));
                throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_INVALID_RESPONSE);
            }

            String textoExtraido = choicesNode.get(0)
                .path("message")
                .path("content")
                .asText("");

            if (textoExtraido.isBlank()) {
                throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_INVALID_RESPONSE);
            }

            return textoExtraido;
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Erro ao interpretar resposta OCR. Corpo: {}", previewTexto(responseJson), ex);
            throw new ExternalServiceException(ErrorMessages.OPENAI_OCR_INVALID_RESPONSE, ex);
        }
    }

    private boolean isTextoOcrUtil(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        String textoLimpo = texto.trim();
        int charsNaoBrancos = textoLimpo.replaceAll("\\s+", "").length();
        if (charsNaoBrancos < MIN_OCR_NON_WHITESPACE_CHARS) {
            return false;
        }

        long palavrasValidas = java.util.Arrays.stream(textoLimpo.split("\\s+"))
            .map(palavra -> palavra.replaceAll("[^\\p{L}\\p{N}]", ""))
            .filter(palavra -> palavra.length() >= 3)
            .count();

        return palavrasValidas >= MIN_OCR_VALID_WORDS;
    }

    private String previewTexto(String texto) {
        if (texto == null) {
            return "";
        }

        String sanitizado = texto.replaceAll("\\s+", " ").trim();
        if (sanitizado.length() <= OCR_PREVIEW_LENGTH) {
            return sanitizado;
        }
        return sanitizado.substring(0, OCR_PREVIEW_LENGTH) + "...";
    }

    private boolean isRespostaDeRecusa(String texto) {
        String normalizado = texto == null ? "" : texto.trim().toLowerCase();
        return normalizado.startsWith("i'm sorry")
            || normalizado.startsWith("sorry,")
            || normalizado.startsWith("desculpe")
            || normalizado.contains("can't assist with that")
            || normalizado.contains("cannot assist with that")
            || normalizado.contains("não posso ajudar com isso");
    }

    private String sanitizarTextoOcr(String texto) {
        String sanitizado = texto == null ? "" : texto.trim();
        sanitizado = sanitizado.replace("```text", "").replace("```", "").trim();
        sanitizado = sanitizado.replaceFirst("(?i)^here is the transcription:\\s*", "");
        sanitizado = sanitizado.replaceFirst("(?i)^transcription:\\s*", "");
        return sanitizado.trim();
    }

    /**
     * @deprecated Use extrairTextoDeImagensOcr(List<String>) em vez disso
     */
    @Deprecated(since = "2.0", forRemoval = true)
    public String extrairTextoDeImagem(String base64Imagem) {
        return extrairTextoDeImagemOcr(base64Imagem);
    }
}
