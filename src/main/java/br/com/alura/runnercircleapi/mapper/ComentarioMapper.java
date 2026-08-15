package br.com.alura.runnercircleapi.mapper;

import br.com.alura.runnercircleapi.dto.AutorResumoDTO;
import br.com.alura.runnercircleapi.dto.ComentarioRequestDTO;
import br.com.alura.runnercircleapi.dto.ComentarioResponseDTO;
import br.com.alura.runnercircleapi.model.Comentario;
import br.com.alura.runnercircleapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapper {

    public ComentarioResponseDTO toResponseDTO(Comentario comentario) {
        return new ComentarioResponseDTO(
                comentario.getId(),
                comentario.getTexto(),
                comentario.getDataCriacao(),
                toAutorResumoDTO(comentario.getAutor())
        );
    }

    private AutorResumoDTO toAutorResumoDTO(User autor) {
        if (autor == null) {
            return null;
        }
        return new AutorResumoDTO(autor.getNome(), autor.getUsername(), autor.getAvatarUrl());
    }

    public Comentario toEntity(ComentarioRequestDTO dto) {
        return new Comentario(dto.texto());
    }
}
