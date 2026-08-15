package br.com.alura.runnercircleapi.dto;

import br.com.alura.runnercircleapi.model.TipoTreino;

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
