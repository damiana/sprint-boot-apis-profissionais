package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.ComentarioRequestDTO;
import br.com.alura.runnercircleapi.mapper.ComentarioMapper;
import br.com.alura.runnercircleapi.model.Comentario;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.ComentarioRepository;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComentarioMapper comentarioMapper;

    public List<Comentario> listarPorTreino(Long treinoId) {
        return comentarioRepository.findByTreinoId(treinoId);
    }

    public Comentario criar(Treino treino, ComentarioRequestDTO dto, Long userId) {
        User autor = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário não encontrado"));

        Comentario comentario = comentarioMapper.toEntity(dto);
        comentario.setAutor(autor);
        comentario.setTreino(treino);

        return comentarioRepository.save(comentario);
    }
}
