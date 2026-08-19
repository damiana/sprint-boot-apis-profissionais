package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.service.TreinoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/treinos")
@Tag(name = "Treinos", description = "CRUD de treinos (corrida e caminhada) do Runner Circle")
public class TreinoController {

    @Autowired
    private TreinoService treinoService;

    @Autowired
    private TreinoMapper treinoMapper;

    @GetMapping
    @Operation(summary = "Lista os treinos de forma paginada, com busca opcional pela descrição")
    public Page<TreinoResponseDTO> listar(@RequestParam(required = false) String busca, Pageable pageable) {
        return treinoService.listar(busca, pageable)
                .map(treinoMapper::toResponseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um treino pelo id")
    public TreinoResponseDTO buscarPorId(@PathVariable Long id) {
        return treinoMapper.toResponseDTO(treinoService.buscarPorId(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cria um novo treino, com imagem opcional (jpg, jpeg, png ou webp, até 5MB). " +
            "O userId é temporário até a autenticação ser implementada.")
    public ResponseEntity<TreinoResponseDTO> criar(@Valid @RequestPart("dados") TreinoRequestDTO dto,
                                                    @RequestPart(value = "imagem", required = false) MultipartFile imagem,
                                                    @RequestParam Long userId) {
        Treino treino = treinoService.criar(dto, userId, imagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(treinoMapper.toResponseDTO(treino));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um treino existente")
    public TreinoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody TreinoRequestDTO dto) {
        return treinoMapper.toResponseDTO(treinoService.atualizar(id, dto));
    }

    @PostMapping("/{id}/curtir")
    @Operation(summary = "Curte um treino. O userId é temporário até a autenticação ser implementada.")
    public TreinoResponseDTO curtir(@PathVariable Long id, @RequestParam Long userId) {
        return treinoMapper.toResponseDTO(treinoService.curtir(id, userId));
    }

    @DeleteMapping("/{id}/curtir")
    @Operation(summary = "Remove a curtida de um treino. O userId é temporário até a autenticação ser implementada.")
    public TreinoResponseDTO descurtir(@PathVariable Long id, @RequestParam Long userId) {
        return treinoMapper.toResponseDTO(treinoService.descurtir(id, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um treino")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        treinoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
