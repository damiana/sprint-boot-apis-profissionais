package br.com.alura.runnercircleapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/treinos")
@Tag(name = "Treinos", description = "CRUD de treinos (corrida e caminhada) do Runner Circle")
public class TreinoController {

    @Autowired
    private TreinoService treinoService;

    @GetMapping
    @Operation(summary = "Lista os treinos, com filtro opcional por tipo (CAMINHADA ou CORRIDA)")
    public List<TreinoResponseDTO> listar(@RequestParam(required = false) TipoTreino tipoTreino) {
        return treinoService.listar(tipoTreino).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um treino pelo id")
    public ResponseEntity<TreinoResponseDTO> buscarPorId(@PathVariable Long id) {
        return treinoService.buscarPorId(id)
                .map(treino -> ResponseEntity.ok(toDTO(treino)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Cria um novo treino")
    public ResponseEntity<TreinoResponseDTO> criar(@Valid @RequestBody TreinoRequestDTO dto) {
        Treino treino = treinoService.criar(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(treino));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um treino existente")
    public ResponseEntity<TreinoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TreinoRequestDTO dto) {
        return treinoService.atualizar(id, dto)
                .map(treino -> ResponseEntity.ok(toDTO(treino)))
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

    private TreinoResponseDTO toDTO(Treino treino) {
        return new TreinoResponseDTO(
                treino.getId(),
                treino.getTipoTreino(),
                treino.getTempoEmMinutos(),
                treino.getDistanciaMetros(),
                treino.getCalorias(),
                treino.getBatimentos(),
                treino.getDescricao(),
                treino.getImagemUrl(),
                treino.getDataCriacao()
        );
    }

    private Treino toEntity(TreinoRequestDTO dto) {
        return new Treino(
                dto.tipoTreino(),
                dto.tempoEmMinutos(),
                dto.distanciaMetros(),
                dto.calorias(),
                dto.batimentos(),
                dto.descricao()
        );
    }
}
