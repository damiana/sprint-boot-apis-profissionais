package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreinoServiceTest {

    @Mock
    private TreinoRepository treinoRepository;

    @InjectMocks
    private TreinoService treinoService;

    @Test
    void buscarTreinoPorId_quandoExiste_retornaTreino() {
        // Arrange
        Long treinoId = 1L;
        Treino treinoExemplo = new Treino(TipoTreino.CORRIDA, 30, 5000, 300, 140, "treino de exemplo");
        treinoExemplo.setId(treinoId);
        when(treinoRepository.findById(treinoId)).thenReturn(Optional.of(treinoExemplo));

        // Act
        Treino resultado = treinoService.buscarPorId(treinoId);

        // Assert
        assertThat(resultado).isSameAs(treinoExemplo);
        assertThat(resultado.getId()).isEqualTo(treinoId);
        assertThat(resultado.getDescricao()).isEqualTo("treino de exemplo");
    }
}
