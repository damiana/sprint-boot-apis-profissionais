package br.com.alura.runnercircleapi.dto;

import br.com.alura.runnercircleapi.model.Role;

public record LoginResponseDTO(
        String token,
        UserResponseDTO usuario,
        Role role
) {
}
