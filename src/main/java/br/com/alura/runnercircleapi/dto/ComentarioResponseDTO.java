package br.com.alura.runnercircleapi.dto;

import java.time.LocalDateTime;

public record ComentarioResponseDTO(
        Long id,
        String texto,
        LocalDateTime dataCriacao,
        AutorResumoDTO autor
) {
}
