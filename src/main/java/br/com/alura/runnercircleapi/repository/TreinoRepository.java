package br.com.alura.runnercircleapi.repository;

import br.com.alura.runnercircleapi.model.Treino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TreinoRepository extends JpaRepository<Treino, Long> {

    List<Treino> findByAutorId(Long autorId);

    @Query("SELECT t FROM Treino t JOIN FETCH t.autor WHERE LOWER(t.descricao) LIKE LOWER(:buscaPattern)")
    Page<Treino> buscarPorDescricaoComAutor(String buscaPattern, Pageable pageable);
}
