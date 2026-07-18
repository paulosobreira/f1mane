package br.flmane.servidor.entidades.persistencia;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * @author Paulo Sobreira Criado em 20/10/2007 as 15:27:53
 */
@Entity
@Table(name = "usuario")
public class JogadorDadosSrv extends FlManeDados implements Serializable {

	@Column(name = "idUsuario", nullable = false, unique = true)
	private String idUsuario;
	private String nome;
	private String imagemJogador;
	private String email;
	private String senha;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "jogadorDadosSrv")
	private List<CorridasDadosSrv> corridas = new LinkedList<CorridasDadosSrv>();
	private long ultimoLogon = 0;
	private long ultimaRecuperacao = 0;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public long getUltimoLogon() {
		return ultimoLogon;
	}

	public void setUltimoLogon(long ultimoLogon) {
		this.ultimoLogon = ultimoLogon;
	}

	public List getCorridas() {
		return corridas;
	}

	public void setCorridas(List corridas) {
		this.corridas = corridas;
	}

	public long getUltimaRecuperacao() {
		return ultimaRecuperacao;
	}

	public void setUltimaRecuperacao(long ultimaRecuperacao) {
		this.ultimaRecuperacao = ultimaRecuperacao;
	}

	public String getImagemJogador() {
		return imagemJogador;
	}

	public void setImagemJogador(String imagemJogador) {
		this.imagemJogador = imagemJogador;
	}

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}
}
