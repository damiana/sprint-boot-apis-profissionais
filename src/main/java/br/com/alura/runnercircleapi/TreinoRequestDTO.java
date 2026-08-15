package br.com.alura.runnercircleapi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TreinoRequestDTO(

        @NotNull(message = "o tipo de treino é obrigatório")
        TipoTreino tipoTreino,

        @NotNull(message = "o tempo em minutos é obrigatório")
        @Positive(message = "o tempo em minutos deve ser maior que zero")
        Integer tempoEmMinutos,

        @NotNull(message = "a distância em metros é obrigatória")
        @Positive(message = "a distância em metros deve ser maior que zero")
        Integer distanciaMetros,

        @NotNull(message = "as calorias são obrigatórias")
        @PositiveOrZero(message = "as calorias não podem ser negativas")
        Integer calorias,

        @NotNull(message = "os batimentos são obrigatórios")
        @Positive(message = "os batimentos devem ser maiores que zero")
        Integer batimentos,

        @NotBlank(message = "a descrição é obrigatória")
        @Size(max = 500, message = "a descrição deve ter no máximo 500 caracteres")
        String descricao
) {
}
