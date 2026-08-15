package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.service.TreinoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/treinos")
@Tag(name = "Treinos", description = "CRUD de treinos (corrida e caminhada) do Runner Circle")
public class TreinoController {

    @Autowired
    private TreinoService treinoService;

    @Autowired
    private TreinoMapper treinoMapper;

    @GetMapping
    @Operation(summary = "Lista os treinos, com filtro opcional por tipo (CAMINHADA ou CORRIDA)")
    public List<TreinoResponseDTO> listar(@RequestParam(required = false) TipoTreino tipoTreino) {
        return treinoService.listar(tipoTreino).stream()
                .map(treinoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um treino pelo id")
    public ResponseEntity<TreinoResponseDTO> buscarPorId(@PathVariable Long id) {
        return treinoService.buscarPorId(id)
                .map(treino -> ResponseEntity.ok(treinoMapper.toResponseDTO(treino)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cria um novo treino, com imagem opcional (jpg, jpeg, png ou webp, até 5MB)")
    public ResponseEntity<TreinoResponseDTO> criar(@Valid @RequestPart("dados") TreinoRequestDTO dto,
                                                    @RequestPart(value = "imagem", required = false) MultipartFile imagem) {
        Treino treino = treinoService.criar(treinoMapper.toEntity(dto), imagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(treinoMapper.toResponseDTO(treino));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um treino existente")
    public ResponseEntity<TreinoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TreinoRequestDTO dto) {
        return treinoService.atualizar(id, dto)
                .map(treino -> ResponseEntity.ok(treinoMapper.toResponseDTO(treino)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um treino")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!treinoService.remover(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
