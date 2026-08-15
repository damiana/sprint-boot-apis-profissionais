package br.com.alura.runnercircleapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TreinoService {

    @Autowired
    private TreinoRepository treinoRepository;

    public List<Treino> listar(TipoTreino tipoTreino) {
        List<Treino> treinos = treinoRepository.findAll();

        if (tipoTreino != null) {
            treinos = treinos.stream()
                    .filter(treino -> treino.getTipoTreino() == tipoTreino)
                    .collect(Collectors.toList());
        }

        return treinos;
    }

    public Optional<Treino> buscarPorId(Long id) {
        return treinoRepository.findById(id);
    }

    public Treino criar(Treino treino) {
        return treinoRepository.save(treino);
    }

    public Optional<Treino> atualizar(Long id, TreinoRequestDTO dto) {
        return treinoRepository.findById(id)
                .map(treino -> {
                    treino.setTipoTreino(dto.tipoTreino());
                    treino.setTempoEmMinutos(dto.tempoEmMinutos());
                    treino.setDistanciaMetros(dto.distanciaMetros());
                    treino.setCalorias(dto.calorias());
                    treino.setBatimentos(dto.batimentos());
                    treino.setDescricao(dto.descricao());
                    return treinoRepository.save(treino);
                });
    }

    public boolean remover(Long id) {
        if (!treinoRepository.existsById(id)) {
            return false;
        }
        treinoRepository.deleteById(id);
        return true;
    }
}
