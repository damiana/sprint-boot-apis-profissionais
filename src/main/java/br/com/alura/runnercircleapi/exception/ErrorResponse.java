package br.com.alura.runnercircleapi.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        String caminho
) {
}
