package br.com.alura.runnercircleapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequestDTO(

        @NotBlank(message = "o texto do comentário é obrigatório")
        @Size(max = 500, message = "o texto deve ter no máximo 500 caracteres")
        String texto
) {
}
