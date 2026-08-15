package br.com.alura.runnercircleapi.repository;

import br.com.alura.runnercircleapi.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreinoRepository extends JpaRepository<Treino, Long> {
}
