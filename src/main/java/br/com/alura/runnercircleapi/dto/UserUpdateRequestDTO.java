package br.com.alura.runnercircleapi.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDTO(

        @NotBlank(message = "o username é obrigatório")
        String username,

        @NotBlank(message = "o nome é obrigatório")
        String nome,

        String bio
) {
}
