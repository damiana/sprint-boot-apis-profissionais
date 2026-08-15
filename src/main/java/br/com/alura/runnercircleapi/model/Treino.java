package br.com.alura.runnercircleapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "treinos")
public class Treino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoTreino tipoTreino;

    private Integer tempoEmMinutos;

    private Integer distanciaMetros;

    private Integer calorias;

    private Integer batimentos;

    private String descricao;

    private String imagemUrl;

    @CreationTimestamp
    private LocalDateTime dataCriacao;

    public Treino() {
    }

    public Treino(TipoTreino tipoTreino, Integer tempoEmMinutos, Integer distanciaMetros,
                  Integer calorias, Integer batimentos, String descricao) {
        this.tipoTreino = tipoTreino;
        this.tempoEmMinutos = tempoEmMinutos;
        this.distanciaMetros = distanciaMetros;
        this.calorias = calorias;
        this.batimentos = batimentos;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoTreino getTipoTreino() {
        return tipoTreino;
    }

    public void setTipoTreino(TipoTreino tipoTreino) {
        this.tipoTreino = tipoTreino;
    }

    public Integer getTempoEmMinutos() {
        return tempoEmMinutos;
    }

    public void setTempoEmMinutos(Integer tempoEmMinutos) {
        this.tempoEmMinutos = tempoEmMinutos;
    }

    public Integer getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(Integer distanciaMetros) {
        this.distanciaMetros = distanciaMetros;
    }

    public Integer getCalorias() {
        return calorias;
    }

    public void setCalorias(Integer calorias) {
        this.calorias = calorias;
    }

    public Integer getBatimentos() {
        return batimentos;
    }

    public void setBatimentos(Integer batimentos) {
        this.batimentos = batimentos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
