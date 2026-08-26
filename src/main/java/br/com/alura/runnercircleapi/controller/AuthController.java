package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.LoginRequestDTO;
import br.com.alura.runnercircleapi.dto.LoginResponseDTO;
import br.com.alura.runnercircleapi.dto.RegisterRequestDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.mapper.UserMapper;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.service.JwtService;
import br.com.alura.runnercircleapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Registro e login de usuários do Runner Circle")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário. A senha é armazenada com hash BCrypt.")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        User user = userService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponseDTO(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário e retorna um token JWT. " +
            "Com lembrarMe=true, o token tem uma expiração maior.")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        User user = userService.autenticar(dto);
        String token = jwtService.gerarToken(user, dto.lembrarMe());
        return ResponseEntity.ok(new LoginResponseDTO(token, userMapper.toResponseDTO(user), user.getRole()));
    }
}
