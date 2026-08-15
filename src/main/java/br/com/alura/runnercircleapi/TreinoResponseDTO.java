package br.com.alura.runnercircleapi;

import java.time.LocalDateTime;

public record TreinoResponseDTO(
        Long id,
        TipoTreino tipoTreino,
        Integer tempoEmMinutos,
        Integer distanciaMetros,
        Integer calorias,
        Integer batimentos,
        String descricao,
        String imagemUrl,
        LocalDateTime dataCriacao
) {
}
