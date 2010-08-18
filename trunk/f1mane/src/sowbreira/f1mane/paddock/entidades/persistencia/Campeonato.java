package sowbreira.f1mane.paddock.entidades.persistencia;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class Campeonato extends F1ManeDados {
	private String temporada;
	private String nivel;
	private Integer qtdeVoltas;
	@JoinColumn(nullable = false)
	private JogadorDadosSrv jogadorDadosSrv;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "campeonato")
	private List<CorridaCampeonato> corridaCampeonatos = new LinkedList<CorridaCampeonato>();

	public String getTemporada() {
		return temporada;
	}

	public void setTemporada(String temporada) {
		this.temporada = temporada;
	}

	public String getNivel() {
		return nivel;
	}

	public void setNivel(String nivel) {
		this.nivel = nivel;
	}

	public Integer getQtdeVoltas() {
		return qtdeVoltas;
	}

	public void setQtdeVoltas(Integer qtdeVoltas) {
		this.qtdeVoltas = qtdeVoltas;
	}

	public List<CorridaCampeonato> getCorridaCampeonatos() {
		return corridaCampeonatos;
	}

	public void setCorridaCampeonatos(List<CorridaCampeonato> corridaCampeonatos) {
		this.corridaCampeonatos = corridaCampeonatos;
	}

}
