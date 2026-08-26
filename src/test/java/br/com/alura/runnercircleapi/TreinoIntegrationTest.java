package br.com.alura.runnercircleapi;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import br.com.alura.runnercircleapi.repository.UserRepository;
import br.com.alura.runnercircleapi.service.TreinoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TreinoIntegrationTest {

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreinoService treinoService;

    @Test
    void criarTreino_persisteEBuscaComDadosCorretos() {
        // Arrange
        User autor = new User("Autor Integração", "autor.integracao", "autor.integracao@email.com", "hash-fake");
        User autorSalvo = userRepository.save(autor);

        TreinoRequestDTO dto = new TreinoRequestDTO(
                TipoTreino.CORRIDA, 30, 5000, 300, 140, "treino de integração"
        );

        // Act
        Treino treinoCriado = treinoService.criar(dto, autorSalvo.getId(), null);
        Optional<Treino> treinoEncontrado = treinoRepository.findById(treinoCriado.getId());

        // Assert
        assertThat(treinoEncontrado).isPresent();

        Treino treino = treinoEncontrado.get();
        assertThat(treino.getTipoTreino()).isEqualTo(dto.tipoTreino());
        assertThat(treino.getTempoEmMinutos()).isEqualTo(dto.tempoEmMinutos());
        assertThat(treino.getDistanciaMetros()).isEqualTo(dto.distanciaMetros());
        assertThat(treino.getCalorias()).isEqualTo(dto.calorias());
        assertThat(treino.getBatimentos()).isEqualTo(dto.batimentos());
        assertThat(treino.getDescricao()).isEqualTo(dto.descricao());
        assertThat(treino.getAutor().getId()).isEqualTo(autorSalvo.getId());
    }
}
