package br.com.alura.runnercircleapi.repository;

import br.com.alura.runnercircleapi.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByTreinoId(Long treinoId);
}
