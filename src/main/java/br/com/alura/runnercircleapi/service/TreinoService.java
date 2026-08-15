package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TreinoService {

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreinoMapper treinoMapper;

    @Autowired
    private ImagemUploadService imagemUploadService;

    public List<Treino> listar(TipoTreino tipoTreino) {
        List<Treino> treinos = treinoRepository.findAll();

        if (tipoTreino != null) {
            treinos = treinos.stream()
                    .filter(treino -> treino.getTipoTreino() == tipoTreino)
                    .collect(Collectors.toList());
        }

        return treinos;
    }

    public Optional<Treino> buscarPorId(Long id) {
        return treinoRepository.findById(id);
    }

    public Treino criar(TreinoRequestDTO dto, Long userId, MultipartFile imagem) {
        User autor = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário não encontrado"));

        Treino treino = treinoMapper.toEntity(dto);
        treino.setAutor(autor);

        if (imagem != null && !imagem.isEmpty()) {
            treino.setImagemUrl(imagemUploadService.salvar(imagem));
        }

        return treinoRepository.save(treino);
    }

    public List<Treino> listarPorAutor(Long autorId) {
        return treinoRepository.findByAutorId(autorId);
    }

    public Optional<Treino> atualizar(Long id, TreinoRequestDTO dto) {
        return treinoRepository.findById(id)
                .map(treino -> {
                    treinoMapper.atualizarEntity(treino, dto);
                    return treinoRepository.save(treino);
                });
    }

    public boolean remover(Long id) {
        if (!treinoRepository.existsById(id)) {
            return false;
        }
        treinoRepository.deleteById(id);
        return true;
    }
}
