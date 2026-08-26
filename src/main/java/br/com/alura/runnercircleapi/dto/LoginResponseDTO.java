package br.com.alura.runnercircleapi.dto;

public record LoginResponseDTO(
        String token,
        UserResponseDTO usuario
) {
}
