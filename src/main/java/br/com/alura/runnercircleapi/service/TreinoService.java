package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.exception.TreinoNotFoundException;
import br.com.alura.runnercircleapi.exception.UsuarioNaoEncontradoException;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TreinoService {

    private static final Logger log = LoggerFactory.getLogger(TreinoService.class);

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreinoMapper treinoMapper;

    @Autowired
    private ImagemUploadService imagemUploadService;

    public Page<Treino> listar(String busca, Pageable pageable) {
        // ordenação customizável fica para a próxima aula; dataCriacao desc é o padrão
        Pageable paginacao = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "dataCriacao"));

        String buscaPattern = "%" + (busca == null ? "" : busca) + "%";
        return treinoRepository.buscarPorDescricaoComAutor(buscaPattern, paginacao);
    }

    public Treino buscarPorId(Long id) {
        return treinoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tentativa de buscar treino inexistente. id={}", id);
                    return new TreinoNotFoundException("treino não encontrado");
                });
    }

    public Treino criar(TreinoRequestDTO dto, Long userId, MultipartFile imagem) {
        User autor = userRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("usuário não encontrado"));

        Treino treino = treinoMapper.toEntity(dto);
        treino.setAutor(autor);

        if (imagem != null && !imagem.isEmpty()) {
            treino.setImagemUrl(imagemUploadService.salvar(imagem));
        }

        Treino treinoSalvo = treinoRepository.save(treino);
        log.info("Treino criado. id={}, autorId={}, tipoTreino={}", treinoSalvo.getId(), userId, treinoSalvo.getTipoTreino());

        return treinoSalvo;
    }

    public List<Treino> listarPorAutor(Long autorId) {
        return treinoRepository.findByAutorId(autorId);
    }

    public Treino curtir(Long treinoId, Long userId) {
        Treino treino = buscarPorId(treinoId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("usuário não encontrado"));

        boolean jaCurtiu = treino.getCurtidas().stream()
                .anyMatch(u -> u.getId().equals(userId));
        if (!jaCurtiu) {
            treino.getCurtidas().add(user);
        }

        return treinoRepository.save(treino);
    }

    public Treino descurtir(Long treinoId, Long userId) {
        Treino treino = buscarPorId(treinoId);
        treino.getCurtidas().removeIf(u -> u.getId().equals(userId));
        return treinoRepository.save(treino);
    }

    public Treino atualizar(Long id, TreinoRequestDTO dto) {
        Treino treino = buscarPorId(id);
        treinoMapper.atualizarEntity(treino, dto);
        return treinoRepository.save(treino);
    }

    public void remover(Long id) {
        Treino treino = buscarPorId(id);
        treinoRepository.delete(treino);
    }
}
