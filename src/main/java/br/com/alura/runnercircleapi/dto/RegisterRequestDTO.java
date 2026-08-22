package br.com.alura.runnercircleapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "o nome é obrigatório")
        String nome,

        @NotBlank(message = "o username é obrigatório")
        String username,

        @NotBlank(message = "o email é obrigatório")
        @Email(message = "o email deve ser válido")
        String email,

        @NotBlank(message = "a senha é obrigatória")
        @Size(min = 8, message = "a senha deve ter no mínimo 8 caracteres")
        String senha
) {
}
