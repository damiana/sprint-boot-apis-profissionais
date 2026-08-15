package br.com.alura.runnercircleapi.repository;

import br.com.alura.runnercircleapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
