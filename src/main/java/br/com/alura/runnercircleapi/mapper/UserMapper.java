package br.com.alura.runnercircleapi.mapper;

import br.com.alura.runnercircleapi.dto.RegisterRequestDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNome(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl()
        );
    }

    public User toEntity(RegisterRequestDTO dto, String senhaHash) {
        return new User(dto.nome(), dto.username(), dto.email(), senhaHash);
    }
}
