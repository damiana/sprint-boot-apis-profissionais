package br.com.alura.runnercircleapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "o email é obrigatório")
        @Email(message = "o email deve ser válido")
        String email,

        @NotBlank(message = "a senha é obrigatória")
        String senha,

        boolean lembrarMe
) {
}
