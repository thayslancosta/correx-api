package com.correx.service.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiCorrecaoResult {

    @JsonProperty("notaFinal")
    private Double notaFinal;

    @JsonProperty("comentarioGeral")
    private String comentarioGeral;

    @JsonProperty("notas")
    private List<NotaCriterioDto> notas;

    @JsonProperty("erros")
    private List<ErroDto> erros;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotaCriterioDto {
        @JsonProperty("nomeCriterio")
        private String nomeCriterio;
        @JsonProperty("peso")
        private Integer peso;
        @JsonProperty("nota")
        private Double nota;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErroDto {
        @JsonProperty("categoria")
        private String categoria;
        @JsonProperty("textoOriginal")
        private String textoOriginal;
        @JsonProperty("textoCorrigido")
        private String textoCorrigido;
    }
}
