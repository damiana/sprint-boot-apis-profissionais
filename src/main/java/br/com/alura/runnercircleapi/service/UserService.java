package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.RegisterRequestDTO;
import br.com.alura.runnercircleapi.mapper.UserMapper;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> buscarPorId(Long id) {
        return userRepository.findById(id);
    }

    public User registrar(RegisterRequestDTO dto) {
        String senhaHash = passwordEncoder.encode(dto.senha());
        User user = userMapper.toEntity(dto, senhaHash);
        return userRepository.save(user);
    }
}
