package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.exception.AcessoNegadoException;
import br.com.alura.runnercircleapi.exception.TreinoNotFoundException;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreinoServiceTest {

    @Mock
    private TreinoRepository treinoRepository;

    @Mock
    private TreinoMapper treinoMapper;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

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

    @Test
    void buscarTreinoPorId_quandoNaoExiste_lancaTreinoNotFoundException() {
        // Arrange
        Long treinoId = 99L;
        when(treinoRepository.findById(treinoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TreinoNotFoundException.class, () -> treinoService.buscarPorId(treinoId));
    }

    @Test
    void atualizarTreino_quandoNaoEhAutor_lancaAcessoNegadoException() {
        // Arrange
        Long treinoId = 1L;

        User autor = new User("Autor", "autor", "autor@email.com", "hash");
        autor.setId(1L);

        User usuarioAutenticado = new User("Outra Pessoa", "outrapessoa", "outra@email.com", "hash");
        usuarioAutenticado.setId(2L);

        Treino treino = new Treino(TipoTreino.CORRIDA, 30, 5000, 300, 140, "descrição original");
        treino.setId(treinoId);
        treino.setAutor(autor);

        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CAMINHADA, 45, 4000, 250, 120, "tentativa de alteração");

        when(treinoRepository.findById(treinoId)).thenReturn(Optional.of(treino));
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);

        // Act & Assert
        assertThrows(AcessoNegadoException.class, () -> treinoService.atualizar(treinoId, dto));
        verify(treinoMapper, never()).atualizarEntity(any(), any());
        verify(treinoRepository, never()).save(any());
    }

    @Test
    void atualizarTreino_quandoEhAutor_atualizaComSucesso() {
        // Arrange
        Long treinoId = 1L;

        User autor = new User("Autor", "autor", "autor@email.com", "hash");
        autor.setId(1L);

        Treino treino = new Treino(TipoTreino.CORRIDA, 30, 5000, 300, 140, "descrição original");
        treino.setId(treinoId);
        treino.setAutor(autor);

        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CAMINHADA, 45, 4000, 250, 120, "descrição atualizada");

        when(treinoRepository.findById(treinoId)).thenReturn(Optional.of(treino));
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autor);
        when(treinoRepository.save(treino)).thenReturn(treino);

        // Act
        Treino resultado = treinoService.atualizar(treinoId, dto);

        // Assert
        verify(treinoMapper).atualizarEntity(treino, dto);
        verify(treinoRepository).save(treino);
        assertThat(resultado).isSameAs(treino);
    }
}
