package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.ComentarioRequestDTO;
import br.com.alura.runnercircleapi.dto.ComentarioResponseDTO;
import br.com.alura.runnercircleapi.mapper.ComentarioMapper;
import br.com.alura.runnercircleapi.model.Comentario;
import br.com.alura.runnercircleapi.service.ComentarioService;
import br.com.alura.runnercircleapi.service.TreinoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/treinos/{treinoId}/comentarios")
@Tag(name = "Comentários", description = "Comentários em treinos do Runner Circle")
public class ComentarioController {

    @Autowired
    private TreinoService treinoService;

    @Autowired
    private ComentarioService comentarioService;

    @Autowired
    private ComentarioMapper comentarioMapper;

    @GetMapping
    @Operation(summary = "Lista os comentários de um treino")
    public ResponseEntity<List<ComentarioResponseDTO>> listar(@PathVariable Long treinoId) {
        return treinoService.buscarPorId(treinoId)
                .map(treino -> ResponseEntity.ok(
                        comentarioService.listarPorTreino(treinoId).stream()
                                .map(comentarioMapper::toResponseDTO)
                                .collect(Collectors.toList())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Cria um comentário em um treino. O userId é temporário até a autenticação ser implementada.")
    public ResponseEntity<ComentarioResponseDTO> criar(@PathVariable Long treinoId,
                                                         @Valid @RequestBody ComentarioRequestDTO dto,
                                                         @RequestParam Long userId) {
        return treinoService.buscarPorId(treinoId)
                .map(treino -> {
                    Comentario comentario = comentarioService.criar(treino, dto, userId);
                    return ResponseEntity.status(HttpStatus.CREATED).body(comentarioMapper.toResponseDTO(comentario));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
