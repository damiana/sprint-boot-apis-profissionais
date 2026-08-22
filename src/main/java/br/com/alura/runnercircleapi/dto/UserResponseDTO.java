package br.com.alura.runnercircleapi.dto;

public record UserResponseDTO(
        Long id,
        String nome,
        String username,
        String email,
        String bio,
        String avatarUrl
) {
}
