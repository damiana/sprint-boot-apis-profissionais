package br.com.alura.runnercircleapi.mapper;

import br.com.alura.runnercircleapi.dto.AutorResumoDTO;
import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class TreinoMapper {

    public TreinoResponseDTO toResponseDTO(Treino treino) {
        return new TreinoResponseDTO(
                treino.getId(),
                treino.getTipoTreino(),
                treino.getTempoEmMinutos(),
                treino.getDistanciaMetros(),
                treino.getCalorias(),
                treino.getBatimentos(),
                treino.getDescricao(),
                treino.getImagemUrl(),
                treino.getDataCriacao(),
                toAutorResumoDTO(treino.getAutor())
        );
    }

    private AutorResumoDTO toAutorResumoDTO(User autor) {
        if (autor == null) {
            return null;
        }
        return new AutorResumoDTO(autor.getNome(), autor.getUsername(), autor.getAvatarUrl());
    }

    public Treino toEntity(TreinoRequestDTO dto) {
        return new Treino(
                dto.tipoTreino(),
                dto.tempoEmMinutos(),
                dto.distanciaMetros(),
                dto.calorias(),
                dto.batimentos(),
                dto.descricao()
        );
    }

    public void atualizarEntity(Treino treino, TreinoRequestDTO dto) {
        treino.setTipoTreino(dto.tipoTreino());
        treino.setTempoEmMinutos(dto.tempoEmMinutos());
        treino.setDistanciaMetros(dto.distanciaMetros());
        treino.setCalorias(dto.calorias());
        treino.setBatimentos(dto.batimentos());
        treino.setDescricao(dto.descricao());
    }
}
