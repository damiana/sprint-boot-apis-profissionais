package br.com.alura.runnercircleapi.repository;

import br.com.alura.runnercircleapi.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TreinoRepository extends JpaRepository<Treino, Long> {

    List<Treino> findByAutorId(Long autorId);

    @Query("SELECT t FROM Treino t JOIN FETCH t.autor ORDER BY t.dataCriacao DESC")
    List<Treino> buscarTodosComAutorOrdenadoPorDataCriacaoDesc();
}
