package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.service.TreinoService;
import br.com.alura.runnercircleapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
