package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.dto.UserUpdateRequestDTO;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.mapper.UserMapper;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.service.TreinoService;
import br.com.alura.runnercircleapi.service.UserService;
import br.com.alura.runnercircleapi.service.UsuarioAutenticadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Operações relacionadas a usuários do Runner Circle")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TreinoService treinoService;

    @Autowired
    private TreinoMapper treinoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todas as pessoas usuárias. Restrito a administradores.")
    public List<UserResponseDTO> listar() {
        return userService.listarTodos().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/me")
    @Operation(summary = "Atualiza os dados da própria pessoa autenticada")
    public UserResponseDTO atualizarMe(@Valid @RequestBody UserUpdateRequestDTO dto) {
        User usuarioAutenticado = usuarioAutenticadoService.obterUsuarioAutenticado();
        User atualizado = userService.atualizar(usuarioAutenticado, dto);
        return userMapper.toResponseDTO(atualizado);
    }

    @GetMapping("/{id}/treinos")
    @Operation(summary = "Lista os treinos criados por um usuário")
    public ResponseEntity<List<TreinoResponseDTO>> listarTreinos(@PathVariable Long id) {
        return userService.buscarPorId(id)
                .map(user -> ResponseEntity.ok(
                        treinoService.listarPorAutor(id).stream()
                                .map(treinoMapper::toResponseDTO)
                                .collect(Collectors.toList())))
                .orElse(ResponseEntity.notFound().build());
    }
}
